package net.n2oapp.platform.selection.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class JoinUtil {

    private JoinUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Для двусторонних отношений типа {@code OneToMany}
     * @param ownerEntities Владелец отношения (в терминах JPA тот, на ком объявлен {@code} JoinColumn)
     * @param fetchChild Функция, достающая дочерние сущности по родительским
     * @param fetcherConstructor Функция, создающая экземпляр класса {@link Fetcher} по дочерней сущности
     * @param getOwnerIdFromChild Функция, возвращающая идентификатор родительской сущности по дочерней сущности
     * @param <ID> Идентификатор родительской сущности
     * @param <F> Тип {@code Fetcher}-а
     * @param <E1> Родительская сущности
     * @param <E2> Дочерняя сущность
     */
    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, List<F>> joinBidirectionalOneToMany(
            Collection<E1> ownerEntities,
            Function<Collection<E1>, Collection<E2>> fetchChild,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E2, ? extends ID> getOwnerIdFromChild
    ) {
        Map<ID, List<F>> result = new HashMap<>();
        Iterable<E2> childEntities = fetchChild.apply(ownerEntities);
        for (E2 child : childEntities) {
            F fetcher = fetcherConstructor.apply(child);
            result.computeIfAbsent(getOwnerIdFromChild.apply(child), ignored -> new ArrayList<>(1)).add(fetcher);
        }
        return result;
    }

    /**
     * Для односторонних отношений типа {@code ToOne}
     * @param ownerEntities Владелец отношения (в терминах JPA тот, на ком объявлен {@code JoinColumn})
     * @param fetchOtherSide Функция, достающая сущности с другой стороны отношения
     * @param fetcherConstructor Функция, возвращающая экземпляр класса {@link Fetcher} по сущности с другой стороны отношения
     * @param getOwnerId Функция, возвращающая идентификатор владельца отношения
     * @param getForeignKey Функция, возвращающая идентификатор, по которому происходит {@code join} отношения
     * @param getOtherSideId Функция, возвращающая идентификатор сущности с другой стороны отношения
     * @param <ID1> Идентификатор владельца отношения
     * @param <ID2> Идентификатор сущности с другой стороны отношения
     * @param <F> Тип {@link Fetcher}
     * @param <E1> Владелец отношения
     * @param <E2> Другая сторона отношения
     */
    public static <ID1, ID2, F extends Fetcher<?>, E1, E2> Map<ID1, F> joinUnidirectionalToOne(
            Collection<E1> ownerEntities,
            Function<Collection<E1>, Collection<E2>> fetchOtherSide,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E1, ? extends ID1> getOwnerId,
            Function<? super E1, ? extends ID2> getForeignKey,
            Function<? super E2, ? extends ID2> getOtherSideId
    ) {
        Map<ID1, F> result = new HashMap<>();
        Map<ID2, E2> joined = fetchOtherSide.apply(ownerEntities).stream().collect(Collectors.toMap(getOtherSideId, Function.identity()));
        for (E1 owner : ownerEntities) {
            ID2 fk = getForeignKey.apply(owner);
            E2 e2 = joined.get(fk);
            if (e2 != null) {
                F fetcher = fetcherConstructor.apply(e2);
                result.put(getOwnerId.apply(owner), fetcher);
            }
        }
        return result;
    }

    /**
     * Метод для односторонних отношений типа {@code ToOne}.<br>
     * В отличие от {@link #joinUnidirectionalToOne}
     * полагается на то, что благодаря {@code prefetch}-у функция {@code getOtherSideFromOwner} не будет делать запрос.<br>
     * Именно так и работает {@code hibernate} для отношений {@code ToOne} в пределах сессии.
     *
     * @param ownerEntities Владелец отношения (в терминах JPA тот, на ком объявлен {@code JoinColumn})
     * @param prefetchOtherSide Функция, которая делает предварительную выборку и сохраняет результаты выборки во владельце отношения
     * @param fetcherConstructor Функция, возвращающий экземпляр класса {@link Fetcher} по другой стороне отношения
     * @param getOtherSideFromOwner Функция, возвращающая другую сторону отношения по владельцу отношения
     * @param getOwnerId Функция, возвращающая идентификатор владельца отношения
     * @param <ID> Идентификатор владельца отношения
     * @param <F> Тип {@link Fetcher}
     * @param <E1> Владелец отношения
     * @param <E2> Другая сторона отношения
     */
    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, F> joinUnidirectionalToOnePrefetching(
            Collection<E1> ownerEntities,
            Consumer<Collection<E1>> prefetchOtherSide,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E1, ? extends E2> getOtherSideFromOwner,
            Function<? super E1, ? extends ID> getOwnerId
    ) {
       prefetchOtherSide.accept(ownerEntities);
       Map<ID, F> result = new HashMap<>();
        for (E1 owner : ownerEntities) {
            E2 prefetchedOtherSide = getOtherSideFromOwner.apply(owner);
            if (prefetchedOtherSide != null) {
                F fetcher = fetcherConstructor.apply(prefetchedOtherSide);
                result.put(getOwnerId.apply(owner), fetcher);
            }
        }
       return result;
    }

}
