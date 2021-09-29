package net.n2oapp.platform.selection.api;

import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        if (resolved == null) {
            return null;
        }
        return resolved.get(0);
    }

    /**
     * Модели дублирующихся {@code fetcher}-ов (для которых {@link Fetcher#getUnderlyingEntity()} вернул одну и ту же сущность)
     * в результирующей коллекции продублированы не будут
     */
    default <C extends Collection<T>> C resolveCollection(
        Collection<? extends F> fetchers,
        S selection,
        Supplier<? extends C> collectionSupplier
    ) {
        if (selection == null) {
            return null;
        }
        Resolution<T, E, ID> resolution = resolveIterable(fetchers, selection, selection.propagation());
        if (resolution == null) {
            return null;
        }
        //noinspection unchecked
        return collectionSupplier.equals(ARRAY_LIST_SUPPLIER) ? (C) resolution.models : resolution.models.stream().collect(Collectors.toCollection(collectionSupplier));
    }

    /**
     * Модели дублирующихся {@code fetcher}-ов (для которых {@link Fetcher#getUnderlyingEntity()} вернул одну и ту же сущность)
     * в результирующем списке продублированы не будут
     */
    default List<T> resolveCollection(Collection<? extends F> fetchers, S selection) {
        //noinspection unchecked
        return resolveCollection(
            fetchers,
            selection,
            ARRAY_LIST_SUPPLIER
        );
    }

    /**
     * @throws IndexOutOfBoundsException если в {@code fetchers} два или более {@code fetcher}-а вернули
     * одну и ту же сущность (метод {@link Fetcher#getUnderlyingEntity()})
     */
    default<I extends Streamable<T>> I resolveStreamable(
        Streamable<? extends F> fetchers,
        S selection
    ) {
        if (selection == null) {
            return null;
        }
        Resolution<T, E, ID> resolution = resolveIterable(fetchers, selection, selection.propagation());
        if (resolution == null) {
            return null;
        }
        final int[] idx = {0};
        Streamable<T> resolved = fetchers.map(
            fetcher -> resolution.models.get(idx[0]++)
        );
        //noinspection unchecked
        return (I) resolved;
    }

    class Resolution<T, E, ID> {

        private static final Resolution<?, ?, ?> EMPTY = new Resolution<>(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            new boolean[] {}
        );

        public final List<E> uniqueEntities;
        public final List<ID> uniqueIds;
        public final List<T> models;
        public final boolean[] duplicate;

        private Resolution(
            final List<E> uniqueEntities,
            final List<ID> uniqueIds,
            final List<T> models,
            final boolean[] duplicate
        ) {
            this.uniqueEntities = uniqueEntities;
            this.uniqueIds = uniqueIds;
            this.models = models;
            this.duplicate = duplicate;
        }

        public static <T, E, ID> Resolution<T, E, ID> from(
            final List<E> uniqueEntities,
            final List<ID> uniqueIds,
            final List<T> models,
            final boolean[] duplicate
        ) {
            return new Resolution<>(uniqueEntities, uniqueIds, models, duplicate);
        }

        @SuppressWarnings("unchecked")
        public static <T, E, ID> Resolution<T, E, ID> empty() {
            return (Resolution<T, E, ID>) EMPTY;
        }

    }

}
