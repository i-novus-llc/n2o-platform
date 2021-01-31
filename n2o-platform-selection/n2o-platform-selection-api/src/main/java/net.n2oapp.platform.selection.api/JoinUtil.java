package net.n2oapp.platform.selection.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
    public static <ID, F extends Fetcher<?>, E1, E2, C extends Collection<F>> Map<ID, C> joinBidirectionalOneToMany(
            Collection<E1> ownerEntities,
            Function<Collection<E1>, Collection<E2>> fetchChild,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E2, ? extends ID> getOwnerIdFromChild,
            Supplier<? extends C> targetCollectionSupplier
    ) {
        Map<ID, C> result = new HashMap<>();
        Iterable<E2> childEntities = fetchChild.apply(ownerEntities);
        for (E2 child : childEntities) {
            F fetcher = fetcherConstructor.apply(child);
            result.computeIfAbsent(getOwnerIdFromChild.apply(child), ignored -> targetCollectionSupplier.get()).add(fetcher);
        }
        return result;
    }

    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, List<F>> joinBidirectionalOneToMany(
        Collection<E1> ownerEntities,
        Function<Collection<E1>, Collection<E2>> fetchChild,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E2, ? extends ID> getOwnerIdFromChild
    ) {
        return joinBidirectionalOneToMany(ownerEntities, fetchChild, fetcherConstructor, getOwnerIdFromChild, ArrayList::new);
    }

    /**
     * Для общего типа отношений ToMany (unidirectional/bidirectional, OneToMany/ManyToMany).
     * Специфичен для JPA
     * @param leftSide Левая сторона отношения
     * @param innerJoin Функция, которая произведет inner join с правой стороной отношения и
     *                  вернет distinct-подмножество {@code leftSide} (отфильтрованное по inner join).<br>
     *                  Ожидается, что после вызова этой функции JPA-провайдер проставит правую сторону отношения
     *                  во все элементы данного подмножества (протестировано на hibernate).<br>
     *                  Например, для отношения (Сотрудник <-> Проект),
     *                  которое является двусторонним ManyToMany отношением<br>
     *                  (сотрудник может работать над несколькими проектами и над проектом работает несколько сотрудников)<br>
     *                  данный JPQL запрос:
     * <pre>
     * &#064;Query("SELECT e FROM Employee e JOIN FETCH e.projects WHERE e IN (?1)")<br>
     * Set&lt;Employee&gt; joinProjects(Collection&lt;Employee&gt; workers);
     * </pre>
     *                  проставит заджойненные Projects во все экземпляры Employee, которые переданы в метод
     * @param fetcherConstructor Функция, возвращающая {@code <F>} по экземпляру с правой стороны отношения
     * @param getLeftSideId Функция, возвращающая идентификатор экземпляра с левой стороны отношения
     * @param getRightSideByLeftSide Функция, возвращающая many-side
     * @param targetCollectionSupplier {@link Supplier}, возвращающий нужный тип коллекции {@code <C>}
     * @param <ID> Тип идентификатора сущностей с левой стороны отношения
     * @param <F> Тип {@link Fetcher}
     * @param <E1> Тип сущностей с левой стороны отношения
     * @param <E2> Тип сущностей с правой стороны отношения
     * @param <C> Тип нужной коллекции
     */
    public static <ID, F extends Fetcher<?>, E1, E2, C extends Collection<F>> Map<ID, C> joinToMany(
        Collection<E1> leftSide,
        Function<Collection<E1>, Set<E1>> innerJoin,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E1, ? extends ID> getLeftSideId,
        Function<? super E1, ? extends Collection<? extends E2>> getRightSideByLeftSide,
        Supplier<? extends C> targetCollectionSupplier
    ) {
        Map<ID, C> result = new HashMap<>();
        Set<E1> joined = innerJoin.apply(leftSide);
        for (E1 leftSideEntity : joined) {
            Collection<? extends E2> rightSide = getRightSideByLeftSide.apply(leftSideEntity);
            ID leftSideId = getLeftSideId.apply(leftSideEntity);
            C manySideFetchers = result.computeIfAbsent(leftSideId, ignored -> targetCollectionSupplier.get());
            for (E2 rightSideEntity : rightSide) {
                manySideFetchers.add(fetcherConstructor.apply(rightSideEntity));
            }
        }
        return result;
    }

    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, List<F>> joinToMany(
        Collection<E1> leftSide,
        Function<Collection<E1>, Set<E1>> innerJoin,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E1, ? extends ID> getLeftSideId,
        Function<? super E1, ? extends Collection<? extends E2>> getRightSideByLeftSide
    ) {
        return joinToMany(leftSide, innerJoin, fetcherConstructor, getLeftSideId, getRightSideByLeftSide, ArrayList::new);
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
