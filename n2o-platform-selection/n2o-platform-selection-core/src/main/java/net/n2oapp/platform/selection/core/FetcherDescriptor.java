package net.n2oapp.platform.selection.core;

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
    final ResolvableType type;
    final Map<String, FetcherAccessor> accessors;

    FetcherDescriptor(ResolvableType type, Map<String, FetcherAccessor> accessors) {
        this.type = type;
        this.accessors = accessors;
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
         * Метод, выбирающий значение из сущности (в случае не вложенной выборки) или
         * проставляющий в DTO значение, которое вернул из
         * {@link FetcherAccessor#nestedFetcherAccessor} (в случае вложенной выборки)
         */
        final Method selectMethod;

        /**
         * Метод, возвращающий вложенный fetcher
         * ({@code null} если данный SelectionKey не является вложенным).
         */
        final Method nestedFetcherAccessor;

        /**
         * {@code true} -- если {@link FetcherAccessor#nestedFetcherAccessor}
         * возвращает коллекцию fetcher-ов
         */
        private final boolean isCollectionFetcher;

        FetcherAccessor(String selectionKey, Method selectMethod, Method nestedFetcherAccessor, boolean isCollectionFetcher) {
            this.selectionKey = selectionKey;
            this.selectMethod = selectMethod;
            this.nestedFetcherAccessor = nestedFetcherAccessor;
            this.isCollectionFetcher = isCollectionFetcher;
        }

        boolean isNested() {
            return nestedFetcherAccessor != null;
        }

        boolean isCollectionFetcher() {
            return isCollectionFetcher;
        }

    }

}
