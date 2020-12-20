package net.n2oapp.platform.selection.core;

import com.google.common.base.Preconditions;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
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
    private static final ConcurrentMap<Map.Entry<Class<? extends Mapper>, SelectionKey>, Method> MAPPER_MAP_METHODS = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Selection>, List<SelectionAccessor>> SELECTION_METHODS = new ConcurrentHashMap<>();

    public static <E> E resolve(Mapper<? extends E> mapper, @NonNull Selection<E> selection) {
        if (mapper == null)
            return null;
        E model = mapper.create();
        if (model == null)
            return null;
        for (SelectionAccessor accessor : getSelectionAccessors(selection)) {
            SelectionEnum select = (SelectionEnum) invoke(accessor.readMethod, selection);
            if (!select.asBoolean())
                continue;
            Method mapperMethod = getMapperMethod(mapper, selection, model, accessor.selectionKey);
            invoke(mapperMethod, mapper, model);
        }
        return model;
    }

    private static <E> Method getMapperMethod(Mapper<? extends E> mapper, Selection<? super E> selection, E model, SelectionKey selectionKey) {
        return MAPPER_MAP_METHODS.computeIfAbsent(Map.entry(mapper.getClass(), selectionKey), entry -> {
            SelectionKey key = entry.getValue();
            for (Method method : mapper.getClass().getMethods()) {
                SelectionKey ann = method.getAnnotation(SelectionKey.class);
                if (ann == null)
                    continue;
                if (ann.value().equals(key.value())) {
                    validateMapperMethod(model, method);
                    return method;
                }
            }
            throw new IllegalStateException("Property '" + key.value() + "' specified in selection of type " + selection.getClass() + " but could be not be found in the mapper of type " + mapper.getClass());
        });
    }

    private static <E> void validateMapperMethod(E model, Method method) {
        Preconditions.checkArgument(
            method.getParameterCount() == 1,
            "Mapper method annotated with @SelectionKey must have exactly one argument. " +
            "Violation in: " + method
        );
        Preconditions.checkArgument(
            method.getParameterTypes()[0].isAssignableFrom(model.getClass()),
            "Mapper's method annotated with @SelectionKey must have argument which type is " +
            "either same or a subclass of type " +
            "returned by this mapper's 'create' method. Violation in: " + method
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
        return SELECTION_METHODS.computeIfAbsent(selection.getClass(), clazz -> {
            Set<SelectionAccessor> result = new HashSet<>(2);
            for (Method method : clazz.getDeclaredMethods()) {
                SelectionKey key = method.getAnnotation(SelectionKey.class);
                if (key == null)
                    continue;
                validateSelectionMethod(method);
                result.add(new SelectionAccessor(method, key));
            }
            ReflectionUtils.doWithFields(clazz, field -> {
                if (field.getAnnotation(SelectionKey.class) != null) {
                    PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, field.getName());
                    Preconditions.checkArgument(descriptor != null && descriptor.getReadMethod() != null, "Field " + field + " annotated with @SelectionKey must have read method (getter) without arguments.");
                    Method method = descriptor.getReadMethod();
                    validateSelectionMethod(method);
                    result.add(new SelectionAccessor(method, field.getAnnotation(SelectionKey.class)));
                }
            });
            return new ArrayList<>(result);
        });
    }

    private static void validateSelectionMethod(Method method) {
        Preconditions.checkArgument(method.getParameterCount() == 0, "Selection's methods annotated with @SelectionKey must have zero arguments. Violation in " + method);
        Preconditions.checkArgument(method.getReturnType() == SelectionEnum.class, "Selection's methods annotated with @SelectionKey must return " + SelectionEnum.class + ". Violation in: " + method);
    }

    private static class SelectionAccessor {

        final Method readMethod;
        final SelectionKey selectionKey;

        SelectionAccessor(Method readMethod, SelectionKey selectionKey) {
            this.readMethod = readMethod;
            this.selectionKey = selectionKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SelectionAccessor)) return false;
            SelectionAccessor that = (SelectionAccessor) o;
            return readMethod.equals(that.readMethod) && selectionKey.equals(that.selectionKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(readMethod, selectionKey);
        }

    }

}
