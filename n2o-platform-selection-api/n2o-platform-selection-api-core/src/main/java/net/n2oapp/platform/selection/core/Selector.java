package net.n2oapp.platform.selection.core;

import com.google.common.base.Preconditions;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Selector {

    private static final ResolvableType MAPPER_RAW = ResolvableType.forRawClass(Mapper.class);
    private static final ResolvableType SELECTION_RAW = ResolvableType.forRawClass(Selection.class);
    private static final ResolvableType COLLECTION_RAW = ResolvableType.forRawClass(Collection.class);

    private Selector() {
    }

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Mapper>, MapperDescriptor> MAPPER_DESCRIPTORS = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Selection>, SelectionDescriptor> SELECTION_DESCRIPTORS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <E> E resolve(Mapper<? extends E> mapper, Selection<E> selection) {
        if (mapper == null || selection == null)
            return null;
        SelectionDescriptor selectionDescriptor = getSelectionDescriptor(selection);
        MapperDescriptor mapperDescriptor = getMapperDescriptor(mapper);
        Preconditions.checkArgument(
            selectionDescriptor.type.isAssignableFrom(mapperDescriptor.type),
            "Selection %s target type is not assignable from mapper %s target type",
            selection.getClass(),
            mapper.getClass()
        );
        E model = mapper.create();
        if (model == null)
            return null;
        for (SelectionDescriptor.SelectionAccessor selectionAccessor : selectionDescriptor.accessors) {
            SelectionEnum select = (SelectionEnum) invoke(selectionAccessor.selectionEnumAccessor, selection);
            if (select == null || !select.asBoolean())
                continue;
            MapperDescriptor.MapperAccessor mapperAccessor = mapperDescriptor.accessors.get(selectionAccessor.selectionKey);
            Preconditions.checkNotNull(
                mapperAccessor,
                "Property %s defined in selection, but mapper is not aware of it. Selection: %s. Mapper: %s",
                selectionAccessor.selectionKey,
                selection.getClass(),
                mapper.getClass()
            );
            if (mapperAccessor.nestedMapperAccessor != null) {
                Preconditions.checkArgument(
                    selectionAccessor.nestedSelectionAccessor != null,
                    "Property %s has nested mapper accessor, but selection is not aware of it. Selection of type %s, mapper of type %s",
                    selectionAccessor.selectionKey,
                    selection.getClass(),
                    mapper.getClass()
                );
                Selection nestedSelection = (Selection) invoke(selectionAccessor.nestedSelectionAccessor, selection);
                if (mapperAccessor.isCollectionMapper) {
                    Collection<Mapper> collection = (Collection) invoke(mapperAccessor.nestedMapperAccessor, mapper);
                    if (CollectionUtils.isEmpty(collection))
                        continue;
                    Collection result;
                    if (collection instanceof List)
                        result = new ArrayList(collection.size());
                    else if (collection instanceof Set)
                        result = new HashSet(collection.size());
                    else
                        throw new IllegalStateException("Unexpected collection of type " + collection.getClass() + " provided");
                    for (Mapper nestedMapper : collection) {
                        result.add(resolve(nestedMapper, nestedSelection));
                    }
                    invoke(mapperAccessor.selectMethod, mapper, model, result);
                } else {
                    Mapper nestedMapper = (Mapper) invoke(mapperAccessor.nestedMapperAccessor, mapper);
                    Object resolve = resolve(nestedMapper, nestedSelection);
                    invoke(mapperAccessor.selectMethod, mapper, model, resolve);
                }
            } else {
                Preconditions.checkArgument(
                    selectionAccessor.nestedSelectionAccessor == null,
                    "Property %s has nested selection, but mapper is not aware of it. Selection of type %s, mapper of type %s",
                    select.getClass(),
                    mapper.getClass()
                );
                invoke(mapperAccessor.selectMethod, mapper, model);
            }
        }
        return model;
    }

    private static Method getMethod(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static MapperDescriptor getMapperDescriptor(Mapper<?> mapper) {
        return MAPPER_DESCRIPTORS.computeIfAbsent(mapper.getClass(), clazz -> {
            ResolvableType type = ResolvableType.forMethodReturnType(getMethod(clazz, "create"));
            Map<String, List<Method>> methods = groupBySelectionKey(clazz);
            Map<String, MapperDescriptor.MapperAccessor> result = new HashMap<>(methods.size());
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method selectMethod = list.get(0);
                if (list.size() == 2) {
                    Method nestedMapperAccessor = list.get(1);
                    ResolvableType nestedReturnType = ResolvableType.forMethodReturnType(nestedMapperAccessor);
                    if (
                        !MAPPER_RAW.isAssignableFrom(nestedReturnType) &&
                        !COLLECTION_RAW.isAssignableFrom(nestedReturnType)
                    ) {
                        Method temp = selectMethod;
                        selectMethod = nestedMapperAccessor;
                        nestedMapperAccessor = temp;
                        list.set(0, selectMethod);
                        list.set(1, nestedMapperAccessor);
                    }
                    nestedReturnType = ResolvableType.forMethodReturnType(nestedMapperAccessor);
                    Preconditions.checkArgument(
                        selectMethod.getParameterCount() == 2,
                        "Mapper's select method must have 2 parameters when nested mapper accessor is present. " +
                        "Violation in %s",
                        selectMethod
                    );
                    ResolvableType secondParam = ResolvableType.forMethodParameter(selectMethod, 1);
                    ResolvableType generic;
                    if (!COLLECTION_RAW.isAssignableFrom(nestedReturnType)) {
                        assertReturnsMapper(nestedMapperAccessor, nestedReturnType);
                        generic = nestedReturnType.getGeneric(0);
                    } else {
                        assertReturnsMapper(nestedMapperAccessor, nestedReturnType.getGeneric(0));
                        secondParam = secondParam.getGeneric(0);
                        generic = nestedReturnType.getGeneric(0, 0);
                    }
                    Preconditions.checkArgument(
                            generic.isAssignableFrom(secondParam),
                            "Mapper's nested mapper accessor return type not assignable to select method second param. " +
                                    "Violation in %s",
                            nestedMapperAccessor
                    );
                } else {
                    Preconditions.checkArgument(
                        selectMethod.getParameterCount() == 1,
                        "Mapper property with no nested mapper accessor must have 1 parameter. Violation in %s",
                        selectMethod
                    );
                }
                checkSelectMethodFirstParam(type, selectMethod);
            }
            for (Map.Entry<String, List<Method>> entry : methods.entrySet()) {
                List<Method> list = entry.getValue();
                Method nestedMapperAccessor = list.size() > 1 ? list.get(1) : null;
                boolean collectionMapper = nestedMapperAccessor != null && Collection.class.isAssignableFrom(nestedMapperAccessor.getReturnType());
                result.put(entry.getKey(), new MapperDescriptor.MapperAccessor(entry.getKey(), list.get(0), nestedMapperAccessor, collectionMapper));
            }
            return new MapperDescriptor(type, result);
        });
    }

    private static void assertReturnsMapper(Method nestedMapperAccessor, ResolvableType returnType) {
        Preconditions.checkArgument(
            MAPPER_RAW.isAssignableFrom(returnType),
            "Mapper's nested mapper accessor must return instance of Mapper class. Violation in %s",
            nestedMapperAccessor
        );
    }

    private static void checkSelectMethodFirstParam(ResolvableType mapperType, Method selectMethod) {
        ResolvableType selectMethodFirstArg = ResolvableType.forMethodParameter(selectMethod, 0);
        Preconditions.checkArgument(
            selectMethodFirstArg.isAssignableFrom(mapperType),
            "Mapper's select method first argument must be the same or a subtype of type returned by 'create' method. " +
            "Violation in %s",
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
