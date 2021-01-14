package net.n2oapp.platform.selection.core;

import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Описание выборки, полученное через Reflection
 */
class SelectionDescriptor {

    /**
     * Тип DTO, для которого предназначена эта выборка
     */
    final ResolvableType type;
    final List<SelectionAccessor> accessors;

    SelectionDescriptor(ResolvableType type, List<SelectionAccessor> accessors) {
        this.type = type;
        this.accessors = accessors;
    }

    /**
     * Доступ к методам выборки, помеченным {@link net.n2oapp.platform.selection.api.SelectionKey}
     */
    static class SelectionAccessor {

        /**
         * @see net.n2oapp.platform.selection.api.SelectionKey
         */
        final String selectionKey;

        /**
         * Метод, возвращающий {@link net.n2oapp.platform.selection.api.SelectionEnum}
         * и, следовательно, определяющий, будет ли выбран данный {@code selectionKey}.
         */
        final Method selectionEnumAccessor;

        /**
         * Метод, возвращающий вложенную выборку ({@code null}, если данный selectionKey не является вложенным)
         */
        final Method nestedSelectionAccessor;

        SelectionAccessor(String selectionKey, Method selectionEnumAccessor, Method nestedSelectionAccessor) {
            this.selectionKey = selectionKey;
            this.selectionEnumAccessor = selectionEnumAccessor;
            this.nestedSelectionAccessor = nestedSelectionAccessor;
        }

    }

}
