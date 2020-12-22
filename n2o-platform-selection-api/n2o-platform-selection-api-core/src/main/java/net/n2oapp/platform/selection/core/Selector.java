package net.n2oapp.platform.selection.core;

import com.google.common.base.Preconditions;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Selector {

    private Selector() {
    }

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Mapper>, Map<String, MapperAccessor>> MAPPER_ACCESSORS = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Selection>, List<SelectionAccessor>> SELECTION_ACCESSORS = new ConcurrentHashMap<>();

    public static <E> E resolve(Mapper<? extends E> mapper, Selection<E> selection) {
        if (mapper == null || selection == null)
            return null;
        E model = mapper.create();
        if (model == null)
            return null;
        assertClass(mapper.getClass());
        assertClass(selection.getClass());
        List<SelectionAccessor> selectionAccessors = getSelectionAccessors(selection);
        Map<String, MapperAccessor> mapperAccessors = getMapperAccessors(model.getClass(), mapper);
        for (SelectionAccessor selectionAccessor : selectionAccessors) {
            SelectionEnum select = (SelectionEnum) invoke(selectionAccessor.selectionEnumAccessor, selection);
            if (select == null || !select.asBoolean())
                continue;
            MapperAccessor mapperAccessor = mapperAccessors.get(selectionAccessor.selectionKey);
            Preconditions.checkNotNull(
                mapperAccessor,
                "Property %s defined in selection of type %s, but could not be found in mapper of type %s",
                selectionAccessor.selectionKey,
                selection.getClass(),
                mapper.getClass()
            );
            if (mapperAccessor.nestedMapperAccessor != null) {
                Preconditions.checkArgument(
                    selectionAccessor.nestedSelectionAccessor != null,
                    "Property %s has nested mapper accessor, but nested selection could not be found. Selection of type %s, mapper of type %s",
                    selectionAccessor.selectionKey,
                    selection.getClass(),
                    mapper.getClass()
                );
                Selection nestedSelection = (Selection) invoke(selectionAccessor.nestedSelectionAccessor, selection);
                if (mapperAccessor.collectionMapper) {
                    Collection<Mapper> collection = (Collection) invoke(mapperAccessor.nestedMapperAccessor, mapper);
                    for (Mapper nestedMapper : collection) {

                    }
                } else {

                }
            } else {
                invoke(mapperAccessor.selectMethod, mapper, model);
            }
        }
        return model;
    }

    private static void assertClass(Class<?> c) {
        Preconditions.checkArgument(!c.isInterface(), "No interface expected. Got: %s", c);
    }

    private static Map<String, MapperAccessor> getMapperAccessors(Class<?> modelClass, Mapper<?> mapper) {
        return MAPPER_ACCESSORS.computeIfAbsent(mapper.getClass(), clazz -> {
            Map<String, List<Method>> methodsRelatedToKey = new HashMap<>();
            doWithMethods(clazz, method -> {
                SelectionKey key = method.getAnnotation(SelectionKey.class);
                if (key == null)
                    return;
                methodsRelatedToKey.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list));
            });
            Map<String, MapperAccessor> result = new HashMap<>(methodsRelatedToKey.size());
            for (Map.Entry<String, List<Method>> entry : methodsRelatedToKey.entrySet()) {
                List<Method> list = entry.getValue();
                Method selectMethod = list.get(0);
                if (list.size() == 2) {
                    Method nestedMapperAccessor = list.get(1);
                    if (!Mapper.class.isAssignableFrom(nestedMapperAccessor.getReturnType()) && !Collection.class.isAssignableFrom(nestedMapperAccessor.getReturnType())) {
                        Method temp = selectMethod;
                        selectMethod = nestedMapperAccessor;
                        nestedMapperAccessor = temp;
                        list.set(0, selectMethod);
                        list.set(1, nestedMapperAccessor);
                    }
                    Preconditions.checkArgument(
                        selectMethod.getParameterCount() == 2,
                        "Mapper's methods annotated with @SelectionKey with nested mapper accessor must have 2 parameters. Violation in %s",
                        selectMethod
                    );
                    checkSelectMethodFirstParam(modelClass, selectMethod);
                    if (!Collection.class.isAssignableFrom(nestedMapperAccessor.getReturnType())) {
                        Preconditions.checkArgument(
                            Mapper.class.isAssignableFrom(nestedMapperAccessor.getReturnType()),
                            "Mapper's nested mapper accessor must return instance of Mapper class. Violation in %s",
                            nestedMapperAccessor
                        );
                    }
                    Preconditions.checkArgument(nestedMapperAccessor.getParameterCount() == 0, "Nested mapper accessor must have zero args. Violation in %s", nestedMapperAccessor);
                } else {
                    checkSelectMethodFirstParam(modelClass, selectMethod);
                    Preconditions.checkArgument(
                        selectMethod.getParameterCount() == 1,
                        "Mapper's methods annotated with @SelectionKey with no nested mapper accessor must have 1 parameter. Violation in %s",
                        selectMethod
                    );
                }
            }
            for (Map.Entry<String, List<Method>> entry : methodsRelatedToKey.entrySet()) {
                List<Method> list = entry.getValue();
                Method nestedMapperAccessor = list.size() > 1 ? list.get(1) : null;
                boolean collectionMapper = nestedMapperAccessor != null && Collection.class.isAssignableFrom(nestedMapperAccessor.getReturnType());
                result.put(entry.getKey(), new MapperAccessor(entry.getKey(), list.get(0), nestedMapperAccessor, collectionMapper));
            }
            return result;
        });
    }

    private static void checkSelectMethodFirstParam(Class<?> modelClass, Method selectMethod) {
        Preconditions.checkArgument(
            selectMethod.getParameterTypes()[0].isAssignableFrom(modelClass),
            "Mapper's select method first argument must be the same or subtype of type returned by 'create' method. Violation in %s",
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

    private static <E> List<SelectionAccessor> getSelectionAccessors(Selection<? super E> selection) {
        return SELECTION_ACCESSORS.computeIfAbsent(selection.getClass(), clazz -> {
            Map<String, List<Method>> methodsRelatedToKey = new HashMap<>();
            doWithMethods(clazz, method -> {
                SelectionKey key = method.getAnnotation(SelectionKey.class);
                if (key == null)
                    return;
                methodsRelatedToKey.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list));
            });
            ReflectionUtils.doWithFields(clazz, field -> getFromField(clazz, methodsRelatedToKey, field));
            List<SelectionAccessor> result = new ArrayList<>(methodsRelatedToKey.size());
            checkSelectionParamsAndReturnTypes(clazz, methodsRelatedToKey);
            for (Map.Entry<String, List<Method>> entry : methodsRelatedToKey.entrySet()) {
                List<Method> list = entry.getValue();
                result.add(new SelectionAccessor(entry.getKey(), list.get(0), list.size() > 1 ? list.get(1) : null));
            }
            return result;
        });
    }

    private static void getFromField(Class<? extends Selection> clazz, Map<String, List<Method>> methodsRelatedToKey, java.lang.reflect.Field field) {
        SelectionKey key = field.getAnnotation(SelectionKey.class);
        if (key != null) {
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, field.getName());
            Preconditions.checkArgument(descriptor != null && descriptor.getReadMethod() != null, "Field " + field + " annotated with @SelectionKey must have read method (getter) without arguments.");
            Method method = descriptor.getReadMethod();
            methodsRelatedToKey.compute(key.value(), (s, list) -> ensureNoDuplicates(method, s, list));
        }
    }

    private static void checkSelectionParamsAndReturnTypes(Class<? extends Selection> clazz, Map<String, List<Method>> methodsRelatedToKey) {
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
                assertReturnsNestedSelection(clazz, entry, nestedSelectionAccessor);
            }
            assertReturnsSelectionEnum(clazz, entry, selectionEnumAccessor);
            assertSelectionMethodZeroParams(selectionEnumAccessor);
        }
    }

    private static void assertReturnsNestedSelection(Class<? extends Selection> clazz, Map.Entry<String, List<Method>> entry, Method nestedSelectionAccessor) {
        Preconditions.checkArgument(
                Selection.class.isAssignableFrom(nestedSelectionAccessor.getReturnType()),
                "No nested selection accessor specified for property %s for type %s",
                entry.getKey(),
                clazz
        );
    }

    private static void assertReturnsSelectionEnum(Class<? extends Selection> clazz, Map.Entry<String, List<Method>> entry, Method selectionEnumAccessor) {
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

    private static class SelectionAccessor {

        final String selectionKey;
        final Method selectionEnumAccessor;
        final Method nestedSelectionAccessor;

        SelectionAccessor(String selectionKey, Method selectionEnumAccessor, Method nestedSelectionAccessor) {
            this.selectionKey = selectionKey;
            this.selectionEnumAccessor = selectionEnumAccessor;
            this.nestedSelectionAccessor = nestedSelectionAccessor;
        }

    }

    private static class MapperAccessor {

        final String selectionKey;
        final Method selectMethod;
        final Method nestedMapperAccessor;
        final boolean collectionMapper;

        private MapperAccessor(String selectionKey, Method selectMethod, Method nestedMapperAccessor, boolean collectionMapper) {
            this.selectionKey = selectionKey;
            this.selectMethod = selectMethod;
            this.nestedMapperAccessor = nestedMapperAccessor;
            this.collectionMapper = collectionMapper;
        }

    }

}
