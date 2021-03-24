package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Fetcher;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JoinUtil {

    private JoinUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Для двусторонних отношений типа {@code OneToMany}, когда {@code leftSide} не является владельцем отношения
     * (в терминах JPA это означает, что на {@code leftSide} объявлено {@code OneToMany(mappedBy = "...")},
     * а на правой стороне {@code JoinColumn})
     * @param leftSide Левая сторона отношения
     * @param innerJoinRightSide Функция, возвращающая правую сторону отношения по левой стороне (inner-join)
     * @param fetcherConstructor Функция, возвращающая {@link Fetcher} {@code <F>} по экземпляру с правой стороны отношения
     * @param getLeftSideIdFromRightSide Функция, возвращающая идентификатор левой стороны отношения по правой стороне отношения (не {@code null})
     * @param <ID> Идентификатор левой стороны отношения
     * @param <F> Тип {@link Fetcher}-а
     * @param <E1> Левая сторона отношения
     * @param <E2> Правая сторона отношения
     */
    public static <ID, F extends Fetcher<?>, E1, E2, C extends Collection<F>> Map<ID, C> joinOneToMany(
            Collection<E1> leftSide,
            Function<Collection<E1>, Collection<E2>> innerJoinRightSide,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E2, ? extends ID> getLeftSideIdFromRightSide,
            Supplier<? extends C> targetCollectionSupplier
    ) {
        Map<ID, C> result = new HashMap<>();
        Collection<E2> childEntities = innerJoinRightSide.apply(leftSide);
        for (E2 child : childEntities) {
            F fetcher = Objects.requireNonNull(fetcherConstructor.apply(child));
            ID leftSideId = Objects.requireNonNull(getLeftSideIdFromRightSide.apply(child), () -> "Missing left side for INNER join for " + child);
            result.computeIfAbsent(leftSideId, ignored -> targetCollectionSupplier.get()).add(fetcher);
        }
        return result;
    }

    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, List<F>> joinOneToMany(
        Collection<E1> leftSide,
        Function<Collection<E1>, Collection<E2>> joinRightSide,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E2, ? extends ID> getLeftSideIdFromRightSide
    ) {
        return joinOneToMany(leftSide, joinRightSide, fetcherConstructor, getLeftSideIdFromRightSide, ArrayList::new);
    }

    /**
     * Для общего типа отношений ToMany (unidirectional/bidirectional, OneToMany/ManyToMany).
     * Специфичен для JPA
     * (для OneToMany отношений лучше использовать {@link #joinOneToMany})
     * @param leftSide Левая сторона отношения
     * @param innerJoinRightSide Функция, которая произведет inner join с правой стороной отношения и
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
        Function<Collection<E1>, Set<E1>> innerJoinRightSide,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E1, ? extends ID> getLeftSideId,
        Function<? super E1, Collection<E2>> getRightSideByLeftSide,
        Supplier<? extends C> targetCollectionSupplier
    ) {
        Map<ID, C> result = new HashMap<>();
        Set<? extends E1> joined = innerJoinRightSide.apply(leftSide);
        for (E1 leftSideEntity : joined) {
            ID leftSideId = Objects.requireNonNull(getLeftSideId.apply(leftSideEntity));
            Collection<? extends E2> rightSide = getRightSideByLeftSide.apply(leftSideEntity);
            Assert.notEmpty(
                rightSide,
                () -> "Empty collection provided for INNER join. Left side entity: " + leftSideEntity + ", ID: " + leftSideId
            );
            checkDuplicates(leftSideId, leftSideEntity, result);
            C manySideFetchers = targetCollectionSupplier.get();
            result.put(leftSideId, manySideFetchers);
            for (E2 rightSideEntity : rightSide) {
                F fetcher = Objects.requireNonNull(fetcherConstructor.apply(rightSideEntity));
                manySideFetchers.add(fetcher);
            }
        }
        return result;
    }

    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, List<F>> joinToMany(
        Collection<E1> leftSide,
        Function<Collection<E1>, Set<E1>> innerJoin,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E1, ? extends ID> getLeftSideId,
        Function<? super E1, Collection<E2>> getRightSideByLeftSide
    ) {
        return joinToMany(leftSide, innerJoin, fetcherConstructor, getLeftSideId, getRightSideByLeftSide, ArrayList::new);
    }

    /**
     * Для отношений типа {@code ToOne}, когда {@code leftSide} является владельцем отношения
     * (в терминах JPA это означает, что на {@code leftSide} объявлен {@code JoinColumn})
     * @param leftSide Левая сторона отношения
     * @param fetchRightSide Функция, возвращающая сущности с правой стороны отношения (inner-join)
     * @param fetcherConstructor Функция, возвращающая экземпляр класса {@link Fetcher} по сущности с правой стороны отношения
     * @param getLeftSideId Функция, возвращающая идентификатор экземпляра с левой стороны отношения
     * @param getForeignKey Функция, возвращающая идентификатор, по которому происходит {@code join} отношения (или {@code null}, если отношения нет)
     * @param getRightSideId Функция, возвращающая идентификатор сущности с правой стороны отношения
     * @param <ID1> Идентификатор сущности с левой стороны отношения
     * @param <ID2> Идентификатор сущности с правой стороны отношения
     * @param <F> Тип {@link Fetcher}
     * @param <E1> Левая сторона отношения
     * @param <E2> Правая сторона отношения
     */
    public static <ID1, ID2, F extends Fetcher<?>, E1, E2> Map<ID1, F> joinToOne(
            Collection<E1> leftSide,
            Function<Collection<E1>, Collection<E2>> fetchRightSide,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E1, ? extends ID1> getLeftSideId,
            Function<? super E1, ? extends ID2> getForeignKey,
            Function<? super E2, ? extends ID2> getRightSideId
    ) {
        Map<ID1, F> result = new HashMap<>();
        Map<ID2, E2> joined = new HashMap<>();
        for (E2 e21 : fetchRightSide.apply(leftSide)) {
            joined.putIfAbsent(getRightSideId.apply(e21), e21);
        }
        for (E1 owner : leftSide) {
            ID2 fk = getForeignKey.apply(owner);
            if (fk != null) {
                E2 e2 = joined.get(fk);
                ID1 leftSideId = Objects.requireNonNull(getLeftSideId.apply(owner));
                checkDuplicates(leftSideId, owner, result);
                Objects.requireNonNull(
                    e2,
                    () -> "Relationship present on " + owner + " for right side via foreign key " + fk + ", but could not be found on joined right side"
                );
                F fetcher = Objects.requireNonNull(fetcherConstructor.apply(e2));
                result.put(leftSideId, fetcher);
            }
        }
        return result;
    }

    /**
     * Метод для отношений типа {@code ToOne}, когда {@code leftSide} является владельцем отношения.
     * (в терминах JPA это означает, что на {@code leftSide} объявлен {@code JoinColumn})<br>
     * В отличие от {@link #joinToOne}
     * полагается на то, что благодаря {@code prefetch}-у функция {@code getOtherSideFromOwner} не будет делать запрос.<br>
     * Именно так и работает {@code hibernate} для отношений {@code ToOne} в пределах сессии.
     *
     * @param leftSide Левая сторона отношения
     * @param prefetchRightSide Функция, которая делает предварительную выборку и сохраняет результаты выборки на левой стороне отношения
     * @param fetcherConstructor Функция, возвращающий экземпляр класса {@link Fetcher} по другой правой стороне отношения
     * @param getOtherSideFromLeftSide Функция, возвращающая другую сторону отношения по владельцу отношения (или {@code null}, если отношения нет)
     * @param getLeftSideId Функция, возвращающая идентификатор владельца отношения
     * @param <ID> Идентификатор владельца отношения
     * @param <F> Тип {@link Fetcher}
     * @param <E1> Владелец отношения
     * @param <E2> Правая сторона отношения
     */
    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, F> joinToOnePrefetching(
            Collection<E1> leftSide,
            Consumer<Collection<E1>> prefetchRightSide,
            Function<? super E2, ? extends F> fetcherConstructor,
            Function<? super E1, ? extends E2> getOtherSideFromLeftSide,
            Function<? super E1, ? extends ID> getLeftSideId
    ) {
       prefetchRightSide.accept(leftSide);
       Map<ID, F> result = new HashMap<>();
        for (E1 owner : leftSide) {
            E2 prefetchedOtherSide = getOtherSideFromLeftSide.apply(owner);
            if (prefetchedOtherSide != null) {
                ID leftSideId = Objects.requireNonNull(getLeftSideId.apply(owner));
                F fetcher = Objects.requireNonNull(fetcherConstructor.apply(prefetchedOtherSide));
                checkDuplicates(leftSideId, owner, result);
                result.put(leftSideId, fetcher);
            }
        }
       return result;
    }

    /**
     * Метод для двусторонних отношений типа OneToOne, когда левая сторона отношения не является его владельцем
     * (в терминах JPA это означает, что на {@code leftSide} отсутствует {@code JoinColumn}).<br>
     * В действительности для JPA этот метод (как и отношение данного типа) имеет малое практическое значение,
     * так как при двустороннем отношении OneToOne правая сторона не может быть LAZY<br>
     * (то есть при загрузке левой стороны отношения используя репозиторий или {@code EntityManager}
     * правая сторона так же всегда будет подгружаться, создавая проблему {@code N+1}).<br>
     * Если только не используется инструментация байт-кода:<br>
     * <a href="https://stackoverflow.com/a/47768154">Ссылка</a>
     * @param leftSide Левая сторона отношения
     * @param fetchRightSide Метод, делающий {@code join} с правой стороной отношения
     * @param fetcherConstructor Метод, возвращающий {@link Fetcher} {@code F} по экземпляру правой стороны отношения
     * @param getLeftSideIdFromRightSide Метод, возвращающий идентификатор левой стороны отношения по правой стороне отношения (или {@code null}, если отношения нет)
     * @param <ID> Идентификатор левой стороны отношения
     * @param <F> Тип {@link Fetcher}
     * @param <E1> Левая сторона отношения
     * @param <E2> Правая сторона отношения
     */
    public static <ID, F extends Fetcher<?>, E1, E2> Map<ID, F> joinOneToOne(
        Collection<E1> leftSide,
        Function<Collection<E1>, Collection<E2>> fetchRightSide,
        Function<? super E2, ? extends F> fetcherConstructor,
        Function<? super E2, ? extends ID> getLeftSideIdFromRightSide
    ) {
        Map<ID, F> result = new HashMap<>();
        Collection<E2> owners = fetchRightSide.apply(leftSide);
        for (E2 owner : owners) {
            ID leftSideId = getLeftSideIdFromRightSide.apply(owner);
            if (leftSideId != null) {
                checkDuplicates(leftSideId, owner, result);
                F fetcher = Objects.requireNonNull(fetcherConstructor.apply(owner));
                result.put(leftSideId, fetcher);
            }
        }
        return result;
    }

    private static void checkDuplicates(Object id, Object entity, Map<?, ?> result) {
        Assert.isTrue(!result.containsKey(id), () -> "Duplicate id " + id + " for entity " + entity);
    }

}
