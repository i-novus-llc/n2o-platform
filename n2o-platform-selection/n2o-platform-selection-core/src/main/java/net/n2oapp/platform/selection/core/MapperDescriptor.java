package net.n2oapp.platform.selection.core;

import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Описание маппера, полученное через Reflection.
 */
class MapperDescriptor {

    /**
     * Тип DTO, для которого этот маппер предназначен.
     */
    final ResolvableType type;
    final Map<String, MapperAccessor> accessors;

    MapperDescriptor(ResolvableType type, Map<String, MapperAccessor> accessors) {
        this.type = type;
        this.accessors = accessors;
    }

    /**
     * Доступ к методам маппера, помеченным {@link net.n2oapp.platform.selection.api.SelectionKey}
     */
    static class MapperAccessor {

        /**
         * @see net.n2oapp.platform.selection.api.SelectionKey
         */
        final String selectionKey;

        /**
         * Метод, выбирающий значение из сущности (в случае не вложенной выборки) или
         * проставляющий в DTO значение, которое вернул из
         * {@link MapperDescriptor.MapperAccessor#nestedMapperAccessor} (в случае вложенной выборки)
         */
        final Method selectMethod;

        /**
         * Метод, возвращающий вложенный маппер
         * ({@code null} если данный SelectionKey не является вложенным).
         */
        final Method nestedMapperAccessor;

        /**
         * {@code true} -- если {@link MapperDescriptor.MapperAccessor#nestedMapperAccessor}
         * возвращает коллекцию мапперов
         */
        private final boolean isCollectionMapper;

        MapperAccessor(String selectionKey, Method selectMethod, Method nestedMapperAccessor, boolean isCollectionMapper) {
            this.selectionKey = selectionKey;
            this.selectMethod = selectMethod;
            this.nestedMapperAccessor = nestedMapperAccessor;
            this.isCollectionMapper = isCollectionMapper;
        }

        boolean isNested() {
            return nestedMapperAccessor != null;
        }

        boolean isCollectionMapper() {
            return isCollectionMapper;
        }

    }

}
