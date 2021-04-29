package net.n2oapp.platform.selection.api;

import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;

/**
 * Группировщик запросов
 * @param <T> Тип модели (DTO)
 * @param <S> Тип выборки {@link Selection}
 * @param <E> Тип отображаемой сущности
 * @param <F> Тип {@link Fetcher}
 * @param <ID> Тип, по которому идентифицируются сущности
 */
public interface Joiner<T, S extends Selection<T>, E, F extends Fetcher<T, S, E>, ID> {

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
        List<T> resolved = resolveCollection(Collections.singleton(fetcher), selection);
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
        Resolution<T, E, ID> resolution = resolveIterable(fetchers, selection, selection.propagation());
        if (resolution == null)
            return null;
        return fetchers.stream().map(fetcher ->
            getFromResolvedMap(resolution.models, fetcher)
        ).collect(toCollection(collectionSupplier));
    }

    default List<T> resolveCollection(Collection<? extends F> fetchers, S selection) {
        return resolveCollection(fetchers, selection, ArrayList::new);
    }

    @SuppressWarnings("unchecked")
    default<I extends Streamable<T>> I resolveStreamable(
        Streamable<? extends F> fetchers,
        S selection
    ) {
        if (selection == null)
            return null;
        Resolution<T, E, ID> resolution = resolveIterable(fetchers, selection, selection.propagation());
        if (resolution == null)
            return null;
        Streamable<T> resolved = fetchers.map(fetcher ->
            getFromResolvedMap(resolution.models, fetcher)
        );
        return (I) resolved;
    }

    private T getFromResolvedMap(Map<ID, T> map, F f) {
        return map.get(Objects.requireNonNull(getId(f.getUnderlyingEntity())));
    }

    class Resolution<T, E, ID> {

        private static final Resolution<?, ?, ?> EMPTY = new Resolution<>(Collections.emptyList(), Collections.emptyMap());

        public final Collection<E> entities;
        @SuppressWarnings("java:S1319")
        public final Map<ID, T> models;

        private Resolution(Collection<E> entities, Map<ID, T> models) {
            this.entities = entities;
            this.models = models;
        }

        public static <T, E, ID> Resolution<T, E, ID> from(
            Collection<E> entities,
            @SuppressWarnings("java:S1319") LinkedHashMap<ID, T> models
        ) {
            return new Resolution<>(entities, models);
        }

        @SuppressWarnings("unchecked")
        public static <T, E, ID> Resolution<T, E, ID> empty() {
            return (Resolution<T, E, ID>) EMPTY;
        }

    }

}
