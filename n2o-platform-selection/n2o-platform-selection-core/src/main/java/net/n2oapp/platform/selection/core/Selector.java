package net.n2oapp.platform.selection.core;

import com.google.common.base.Preconditions;
import net.n2oapp.platform.selection.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import static net.n2oapp.platform.selection.api.SelectionPropagationEnum.*;

/**
 * Основная логика по выборочному отображению.
 */
public final class Selector {

    private static final String VIOLATION = "Violation in %s";

    private static final ResolvableType FETCHER_RAW = ResolvableType.forRawClass(Fetcher.class);
    private static final ResolvableType SELECTION_RAW = ResolvableType.forRawClass(Selection.class);
    private static final ResolvableType COLLECTION_RAW = ResolvableType.forRawClass(Collection.class);
    private static final String CREATE_METHOD = "create";

    private Selector() {
    }

    /**
     * Кешированные дескрипторы fetcher-ов
     */
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Fetcher>, FetcherDescriptor> FETCHER_DESCRIPTORS = new ConcurrentHashMap<>();

    /**
     * Кешированные дескрипторы выборок
     */
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Selection>, SelectionDescriptor> SELECTION_DESCRIPTORS = new ConcurrentHashMap<>();

    /**
     * @param srcPage Page fetcher-ов
     * @param selection Выборка
     * @param <E> Тип DTO
     * @return Page DTO, чьи поля выборочно заполнены в соответствии с {@code selection}
     */
    public static <E> Page<E> resolvePage(Page<? extends Fetcher<? extends E>> srcPage, Selection<? extends E> selection) {
        return srcPage.map(fetcher -> resolve(fetcher, selection));
    }

    /**
     * @param fetcher Fetcher
     * @param selection Выборка
     * @param <E> Тип DTO
     * @return Выборочное отображение E в соответствии с {@code selection}
     * @throws IllegalArgumentException если целевой тип DTO {@code fetcher}-а нельзя присвоить целевому типу DTO {@code selection}-а,
     * @throws IllegalArgumentException если в {@code selection} присутствует selectionKey и отсутствует в {@code fetcher}-е.
     * @throws IllegalArgumentException если в {@code fetcher}-е некоторый selectionKey является вложенным, но в {@code selection}-е нет (и наоборот)
     * @throws IllegalStateException если {@code selectionKey} {@code fetcher} - а вернул коллекцию fetcher-ов неподдерживаемого типа
     * @throws IllegalArgumentException
     *              если переданные реализации {@code fetcher} и {@code selection} не соответствуют контрактам,
     *              описанным в {@link Fetcher} и {@link net.n2oapp.platform.selection.api.Selection} соответственно
     */
    @SuppressWarnings("rawtypes")
    public static <E> E resolve(Fetcher<? extends E> fetcher, Selection<? extends E> selection) {
        if (fetcher == null || selection == null)
            return null;
        SelectionDescriptor selectionDescriptor = getSelectionDescriptor(selection);
        FetcherDescriptor fetcherDescriptor = getFetcherDescriptor(fetcher);
        Preconditions.checkArgument(
            selectionDescriptor.type.isAssignableFrom(fetcherDescriptor.type),
            "Selection %s target type is not assignable from fetcher %s target type",
            selection.getClass(),
            fetcher.getClass()
        );
        SelectionPropagationEnum propagation = NORMAL;
        if (selection.propagation() != null)
            propagation = selection.propagation();
        if (propagation == NESTED || propagation == ALL) {
            return selectAll(fetcher, fetcherDescriptor, propagation);
        } else {
            E model = fetcher.create();
            if (model == null)
                return null;
            for (SelectionDescriptor.SelectionAccessor selectionAccessor : selectionDescriptor.accessors) {
                SelectionEnum select = (SelectionEnum) invoke(selectionAccessor.selectionEnumAccessor, selection);
                if (select == null || !select.asBoolean())
                    continue;
                FetcherDescriptor.FetcherAccessor fetcherAccessor = fetcherDescriptor.accessors.get(selectionAccessor.selectionKey);
                Preconditions.checkArgument(
                        fetcherAccessor != null,
                        "Property %s defined in selection, but fetcher is unaware of it. Selection: %s. Fetcher: %s",
                        selectionAccessor.selectionKey,
                        selection.getClass(),
                        fetcher.getClass()
                );
                if (fetcherAccessor.isNested()) {
                    Preconditions.checkArgument(
                            selectionAccessor.isNested(),
                            "Property %s has nested fetcher accessor, but selection is unaware of it. Selection of type %s, fetcher of type %s",
                            selectionAccessor.selectionKey,
                            selection.getClass(),
                            fetcher.getClass()
                    );
                    Selection nestedSelection = (Selection) invoke(selectionAccessor.nestedSelectionAccessor, selection);
                    nestedSelection(fetcher, model, fetcherAccessor, nestedSelection, Selector::resolve);
                } else {
                    Preconditions.checkArgument(
                            !selectionAccessor.isNested(),
                            "Property %s has nested selection, but fetcher is unaware of it. Selection of type %s, fetcher of type %s",
                            selection.getClass(),
                            fetcher.getClass()
                    );
                    invoke(fetcherAccessor.selectMethod, fetcher, model);
                }
            }
            return model;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E> void nestedSelection(
            Fetcher<? extends E> fetcher,
            E model,
            FetcherDescriptor.FetcherAccessor fetcherAccessor,
            Selection nestedSelection,
            BiFunction<Fetcher<? extends E>, Selection, Object> nestedResolve
    ) {
        if (fetcherAccessor.isCollectionFetcher()) {
            Collection<Fetcher> collection = (Collection) invoke(fetcherAccessor.nestedFetcherAccessor, fetcher);
            if (CollectionUtils.isEmpty(collection))
                return;
            Collection result;
            if (collection instanceof List)
                result = new ArrayList(collection.size());
            else if (collection instanceof Set)
                result = new HashSet(collection.size());
            else
                throw new IllegalStateException("Unexpected collection of type " + collection.getClass() + " provided");
            for (Fetcher nestedFetcher : collection) {
                result.add(nestedResolve.apply(nestedFetcher, nestedSelection));
            }
            invoke(fetcherAccessor.selectMethod, fetcher, model, result);
        } else {
            Fetcher nestedFetcher = (Fetcher) invoke(fetcherAccessor.nestedFetcherAccessor, fetcher);
            Object resolve = nestedResolve.apply(nestedFetcher, nestedSelection);
            if (resolve != null)
                invoke(fetcherAccessor.selectMethod, fetcher, model, resolve);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> E selectAll(Fetcher<? extends E> fetcher, FetcherDescriptor fetcherDescriptor, SelectionPropagationEnum propagation) {
        if (fetcher == null)
            return null;
        E model = fetcher.create();
        if (model == null)
            return null;
        for (Map.Entry<String, FetcherDescriptor.FetcherAccessor> entry : fetcherDescriptor.accessors.entrySet()) {
            FetcherDescriptor.FetcherAccessor fetcherAccessor = entry.getValue();
            if (!fetcherAccessor.isNested())
                invoke(fetcherAccessor.selectMethod, fetcher, model);
            else {
                nestedSelection(fetcher, model, fetcherAccessor, null, (nestedFetcher, selection) -> {
                    if (nestedFetcher == null)
                        return null;
                    switch (propagation) {
                        case ALL:
                            return resolve(nestedFetcher, selection);
                        case NESTED:
                            return selectAll(nestedFetcher, getFetcherDescriptor(nestedFetcher), propagation);
                        default:
                            throw new IllegalStateException("Unexpected propagation of type " + propagation);
                    }
                });
            }
        }
        return model;
    }

    private static Method getMethod(Class<?> clazz, String name) {
        try {
            Method method = clazz.getMethod(name);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Method '" + name + "' cannot be found on " + clazz);
        }
    }

    private static FetcherDescriptor getFetcherDescriptor(Fetcher<?> fetcher) {
        return FETCHER_DESCRIPTORS.computeIfAbsent(fetcher.getClass(), clazz -> {
            ResolvableType type = ResolvableType.forMethodReturnType(getMethod(clazz, CREATE_METHOD));
            Map<String, List<Method>> methods = groupBySelectionKey(clazz);
            Map<String, FetcherDescriptor.FetcherAccessor> result = new HashMap<>(methods.size());
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method selectMethod = list.get(0);
                if (list.size() == 2) {
                    Method nestedFetcherAccessor = list.get(1);
                    ResolvableType nestedReturnType = ResolvableType.forMethodReturnType(nestedFetcherAccessor);
                    if (
                        !FETCHER_RAW.isAssignableFrom(nestedReturnType) &&
                        !COLLECTION_RAW.isAssignableFrom(nestedReturnType)
                    ) {
                        Method temp = selectMethod;
                        selectMethod = nestedFetcherAccessor;
                        nestedFetcherAccessor = temp;
                        list.set(0, selectMethod);
                        list.set(1, nestedFetcherAccessor);
                    }
                    nestedReturnType = ResolvableType.forMethodReturnType(nestedFetcherAccessor);
                    Preconditions.checkArgument(
                        selectMethod.getParameterCount() == 2,
                        "Fetcher's select method must have 2 parameters when nested fetcher accessor is present. " +
                        VIOLATION,
                        selectMethod
                    );
                    ResolvableType secondParam = ResolvableType.forMethodParameter(selectMethod, 1);
                    ResolvableType generic;
                    if (!COLLECTION_RAW.isAssignableFrom(nestedReturnType)) {
                        assertReturnsFetcher(nestedFetcherAccessor, nestedReturnType);
                        generic = ResolvableType.forMethodReturnType(getMethod(nestedReturnType.resolve(Object.class), CREATE_METHOD));
                    } else {
                        assertReturnsFetcher(nestedFetcherAccessor, nestedReturnType.getGeneric(0));
                        assertCollection(secondParam, selectMethod);
                        secondParam = secondParam.getGeneric(0);
                        generic = ResolvableType.forMethodReturnType(getMethod(nestedReturnType.getGeneric(0).resolve(Object.class), CREATE_METHOD));
                    }
                    Preconditions.checkArgument(
                            generic.isAssignableFrom(secondParam),
                            "Fetcher's nested fetcher accessor return type not assignable to select method second param. " +
                            VIOLATION,
                            nestedFetcherAccessor
                    );
                } else {
                    Preconditions.checkArgument(
                        selectMethod.getParameterCount() == 1,
                        "Fetcher property with no nested fetcher accessor must have 1 parameter. Violation in %s",
                        selectMethod
                    );
                }
                checkSelectMethodFirstParam(type, selectMethod);
            }
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method nestedFetcherAccessor = list.size() > 1 ? list.get(1) : null;
                boolean collectionFetcher = nestedFetcherAccessor != null && Collection.class.isAssignableFrom(nestedFetcherAccessor.getReturnType());
                result.put(entry.getKey(), new FetcherDescriptor.FetcherAccessor(entry.getKey(), list.get(0), nestedFetcherAccessor, collectionFetcher));
            }
            return new FetcherDescriptor(type, result);
        });
    }

    private static void assertCollection(ResolvableType param, Method selectMethod) {
        Preconditions.checkArgument(COLLECTION_RAW.isAssignableFrom(param),
            "Second param of select method must be of collection type when nested fetcher accessor is present" +
            VIOLATION,
            selectMethod
        );
    }

    private static void assertReturnsFetcher(Method nestedFetcherAccessor, ResolvableType returnType) {
        Preconditions.checkArgument(
            FETCHER_RAW.isAssignableFrom(returnType),
            "Fetcher's nested fetcher accessor must return instance of Fetcher class. Violation in %s",
            nestedFetcherAccessor
        );
    }

    private static void checkSelectMethodFirstParam(ResolvableType fetcherType, Method selectMethod) {
        ResolvableType selectMethodFirstArg = ResolvableType.forMethodParameter(selectMethod, 0);
        Preconditions.checkArgument(
            selectMethodFirstArg.isAssignableFrom(fetcherType),
            "Fetcher's select method first argument must be the same, or a subtype of type returned by 'create' method. " +
            VIOLATION,
            selectMethod
        );
    }

    private static Object invoke(Method m, Object obj, Object...args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
//          Normally will not fail with IllegalAccessException because we're accessing public method
            throw new IllegalStateException("Can't access method " + m, e);
        }
    }

    private static SelectionDescriptor getSelectionDescriptor(Selection<?> selection) {
        return SELECTION_DESCRIPTORS.computeIfAbsent(selection.getClass(), clazz -> {
            Map<String, List<Method>> methods = groupBySelectionKey(clazz);
            ReflectionUtils.doWithFields(clazz, field -> getFromField(clazz, methods, field));
            List<SelectionDescriptor.SelectionAccessor> result = new ArrayList<>(methods.size());
            checkSelection(clazz, methods);
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                result.add(new SelectionDescriptor.SelectionAccessor(entry.getKey(), list.get(0), list.size() > 1 ? list.get(1) : null));
            }
            ResolvableType type = ResolvableType.forMethodReturnType(getMethod(clazz, "typeMarker"));
            return new SelectionDescriptor(type, result);
        });
    }

    private static Map<String, List<Method>> groupBySelectionKey(Class<?> clazz) {
        Map<String, List<Method>> methodsRelatedToKey = new HashMap<>();
        doWithMethods(clazz, method -> {
            SelectionKey key = method.getAnnotation(SelectionKey.class);
            if (key == null)
                return;
            methodsRelatedToKey.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list));
        });
        return methodsRelatedToKey;
    }

    private static void getFromField(Class<?> clazz, Map<String, List<Method>> methods, java.lang.reflect.Field field) {
        SelectionKey key = field.getAnnotation(SelectionKey.class);
        if (key != null) {
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, field.getName());
            Preconditions.checkArgument(
                descriptor != null && descriptor.getReadMethod() != null,
                "Field %s annotated with @SelectionKey must have read method (getter) without arguments.",
                field
            );
            Method method = descriptor.getReadMethod();
            methods.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list));
        }
    }

    private static void checkSelection(Class<?> clazz, Map<String, List<Method>> methodsRelatedToKey) {
        for (Map.Entry<String, List<Method>> entry : methodsRelatedToKey.entrySet()) {
            List<Method> list = entry.getValue();
            Method selectionEnumAccessor = list.get(0);
            if (list.size() == 2) {
                Method nestedSelectionAccessor = list.get(1);
                if (selectionEnumAccessor.getReturnType() != SelectionEnum.class) {
                    Method temp = selectionEnumAccessor;
                    selectionEnumAccessor = nestedSelectionAccessor;
                    nestedSelectionAccessor = temp;
                    list.set(0, selectionEnumAccessor);
                    list.set(1, nestedSelectionAccessor);
                }
                assertSelectionMethodZeroParams(nestedSelectionAccessor);
                assertReturnsSelection(clazz, entry, nestedSelectionAccessor);
            }
            assertReturnsSelectionEnum(clazz, entry, selectionEnumAccessor);
            assertSelectionMethodZeroParams(selectionEnumAccessor);
        }
    }

    private static void assertReturnsSelection(Class<?> clazz, Map.Entry<String, List<Method>> entry, Method nestedSelectionAccessor) {
        Preconditions.checkArgument(
            SELECTION_RAW.isAssignableFrom(ResolvableType.forMethodReturnType(nestedSelectionAccessor)),
            "No nested selection accessor specified for property %s for type %s",
            entry.getKey(),
            clazz
        );
    }

    private static void assertReturnsSelectionEnum(Class<?> clazz, Map.Entry<String, List<Method>> entry, Method selectionEnumAccessor) {
        Preconditions.checkArgument(
            selectionEnumAccessor.getReturnType() == SelectionEnum.class,
            "No SelectionEnum accessor found for property %s in selection of type %s",
            entry.getKey(),
            clazz
        );
    }

    private static void assertSelectionMethodZeroParams(Method selectionEnumAccessor) {
        Preconditions.checkArgument(
            selectionEnumAccessor.getParameterCount() == 0,
            "Selection's methods annotated with @SelectionKey must have zero arguments. Violation in %s",
            selectionEnumAccessor
        );
    }

    private static List<Method> ensureNoDuplicates(Method method, String key, List<Method> list) {
        if (list == null)
            return new ArrayList<>(Collections.singletonList(method));
        if (list.contains(method))
            return list;
        Preconditions.checkArgument(list.size() < 2, "Key %s meets more than maximum of 2 times", key);
        list.add(method);
        return list;
    }

    private static void doWithMethods(Class<?> clazz, ReflectionUtils.MethodCallback callback) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            try {
                callback.doWith(method);
                method.setAccessible(true);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            doWithMethods(clazz.getSuperclass(), callback);
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            doWithMethods(ifc, callback);
        }
    }

}
