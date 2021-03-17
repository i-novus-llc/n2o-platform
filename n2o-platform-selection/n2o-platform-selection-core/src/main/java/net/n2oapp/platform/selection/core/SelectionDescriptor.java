package net.n2oapp.platform.selection.core;

import com.google.common.base.Preconditions;
import net.n2oapp.platform.selection.api.Selection;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Описание выборки, полученное через Reflection
 */
class SelectionDescriptor {

    /**
     * Тип DTO, для которого предназначена эта выборка
     */
    final ResolvableType targetType;
    final Map<String, SelectionAccessor> accessors;
    final Class<? extends Selection> selectionClass;

    SelectionDescriptor(ResolvableType targetType, Map<String, SelectionAccessor> accessors, Class<? extends Selection> selectionClass) {
        this.targetType = targetType;
        this.accessors = accessors;
        this.selectionClass = selectionClass;
    }

    /**
     * Доступ к методам выборки, помеченным {@link net.n2oapp.platform.selection.api.SelectionKey}
     */
    static class SelectionAccessor {

        /**
         * Метод, возвращающий {@link net.n2oapp.platform.selection.api.SelectionEnum}
         * и, следовательно, определяющий, будет ли выбран данный {@code selectionKey}.
         */
        final Method selectionEnumAccessor;

        /**
         * Метод, возвращающий вложенную выборку ({@code null}, если данный selectionKey не является вложенным)
         */
        final Method nestedSelectionAccessor;

        SelectionAccessor(Method selectionEnumAccessor, Method nestedSelectionAccessor) {
            Preconditions.checkArgument(
                (selectionEnumAccessor != null && nestedSelectionAccessor == null) ||
                (selectionEnumAccessor == null && nestedSelectionAccessor != null)
            );
            this.selectionEnumAccessor = selectionEnumAccessor;
            this.nestedSelectionAccessor = nestedSelectionAccessor;
        }

        boolean isNested() {
            return nestedSelectionAccessor != null;
        }

    }

}
