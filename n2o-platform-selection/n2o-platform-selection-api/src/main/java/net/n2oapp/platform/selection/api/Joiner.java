package net.n2oapp.platform.selection.api;

import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Группировщик запросов
 * @param <T> Тип модели (DTO)
 * @param <S> Тип выборки {@link Selection}
 * @param <E> Тип отображаемой сущности
 * @param <F> Тип {@link Fetcher}
 * @param <ID> Тип, по которому идентифицируются сущности
 */
public interface Joiner<T, S extends Selection<T>, E, F extends Fetcher<T, S, E>, ID> {

    @SuppressWarnings("rawtypes")
    Supplier<ArrayList> ARRAY_LIST_SUPPLIER = ArrayList::new;

    /**
     * @return Идентификатор отображаемой сущности
     */
    @NonNull
    ID getId(E entity);

    /**
     * Данный метод не должен использоваться напрямую.
     * Вместо него следует использовать методы {@code resolve*}, определенные ниже
     */
    Joiner.Resolution<T, E, ID> resolveIterable(Iterable<? extends F> fetchers, S selection, SelectionPropagation propagation);

    default T resolve(F fetcher, S selection) {
        List<T> resolved = resolveCollection(Collections.singletonList(fetcher), selection);
        if (resolved == null)
            return null;
        return resolved.get(0);
    }

    default <C extends Collection<T>> C resolveCollection(
        Collection<? extends F> fetchers,
        S selection,
        Supplier<? extends C> collectionSupplier
    ) {
        if (selection == null)
            return null;
        Fetcher.CTX.set(new IdentityHashMap<>());
        try {
            Resolution<T, E, ID> resolution = resolveIterable(fetchers, selection, selection.propagation());
            if (resolution == null)
                return null;
            //noinspection unchecked
            return collectionSupplier.equals(ARRAY_LIST_SUPPLIER) ? (C) resolution.models : resolution.models.stream().collect(Collectors.toCollection(collectionSupplier));
        } finally {
            Fetcher.CTX.remove();
        }
    }

    default List<T> resolveCollection(Collection<? extends F> fetchers, S selection) {
        //noinspection unchecked
        return resolveCollection(
            fetchers,
            selection,
            ARRAY_LIST_SUPPLIER
        );
    }

    default<I extends Streamable<T>> I resolveStreamable(
        Streamable<? extends F> fetchers,
        S selection
    ) {
        if (selection == null)
            return null;
        Fetcher.CTX.set(new IdentityHashMap<>());
        try {
            Resolution<T, E, ID> resolution = resolveIterable(fetchers, selection, selection.propagation());
            if (resolution == null)
                return null;
            final int[] idx = {0};
            Streamable<T> resolved = fetchers.map(
                fetcher -> resolution.models.get(idx[0]++)
            );
            //noinspection unchecked
            return (I) resolved;
        } finally {
            Fetcher.CTX.remove();
        }
    }

    class Resolution<T, E, ID> {

        private static final Resolution<?, ?, ?> EMPTY = new Resolution<>(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            new boolean[] {}
        );

        public final List<E> unresolvedEntities;
        public final List<ID> unresolvedIds;
        public final List<ID> allIds;
        public final List<T> models;
        public final boolean[] resolvedIds;

        private Resolution(
            final List<E> unresolvedEntities,
            final List<ID> allIds,
            final List<T> models,
            final boolean[] resolvedIds
        ) {
            this.unresolvedEntities = unresolvedEntities;
            this.allIds = allIds;
            this.models = models;
            this.resolvedIds = resolvedIds;
            this.unresolvedIds = new FilteredImmutableList<>(resolvedIds, allIds);
        }

        public static <T, E, ID> Resolution<T, E, ID> from(
            final List<E> unresolvedEntities,
            final List<ID> allIds,
            final List<T> models,
            final boolean[] resolvedIds
        ) {
            return new Resolution<>(unresolvedEntities, allIds, models, resolvedIds);
        }

        @SuppressWarnings("unchecked")
        public static <T, E, ID> Resolution<T, E, ID> empty() {
            return (Resolution<T, E, ID>) EMPTY;
        }

    }

}
