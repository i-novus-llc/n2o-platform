package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Fetcher;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Описание fetcher-а, полученное через Reflection.
 */
class FetcherDescriptor {

    /**
     * Тип DTO, для которого этот fetcher предназначен.
     */
    final ResolvableType targetType;
    final Map<String, FetcherAccessor> accessors;
    final Class<? extends Fetcher> fetcherClass;

    FetcherDescriptor(ResolvableType targetType, Map<String, FetcherAccessor> accessors, Class<? extends Fetcher> fetcherClass) {
        this.targetType = targetType;
        this.accessors = accessors;
        this.fetcherClass = fetcherClass;
    }

    /**
     * Доступ к методам fetcher-а, помеченным {@link net.n2oapp.platform.selection.api.SelectionKey}
     */
    static class FetcherAccessor {

        /**
         * @see net.n2oapp.platform.selection.api.SelectionKey
         */
        final String selectionKey;

        /**
         * Метод, достающий значение из сущности (в случае не вложенной выборки) или
         * проставляющий в DTO значение, которое вернул из
         * {@link FetcherAccessor#nestedFetcherAccessor} (в случае вложенной выборки)
         */
        final Method fetchMethod;

        /**
         * Метод, возвращающий вложенный fetcher
         * ({@code null} если данный SelectionKey не является вложенным).
         */
        final Method nestedFetcherAccessor;

        /**
         * Если {@link FetcherAccessor#nestedFetcherAccessor}
         * возвращает коллекцию fetcher-ов
         */
        final ResolvableType collectionType;

        FetcherAccessor(String selectionKey, Method fetchMethod, Method nestedFetcherAccessor, ResolvableType collectionType) {
            this.selectionKey = selectionKey;
            this.fetchMethod = fetchMethod;
            this.nestedFetcherAccessor = nestedFetcherAccessor;
            this.collectionType = collectionType;
        }

        boolean isNested() {
            return nestedFetcherAccessor != null;
        }

        boolean isToManyAssociation() {
            return collectionType != null;
        }

    }

}
