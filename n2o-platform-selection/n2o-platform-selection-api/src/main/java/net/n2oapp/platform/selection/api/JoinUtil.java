package net.n2oapp.platform.selection.api;

import org.springframework.util.Assert;

import java.util.*;
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
     * @param innerJoinRightSide Функция, возвращающая правую сторону отношения по левой стороне (inner-join)
     * @param fetcherConstructor Функция, возвращающая {@link Fetcher} {@code <F>} по экземпляру с правой стороны отношения
     * @param getLeftSideIdFromRightSide Функция, возвращающая идентификатор левой стороны отношения по правой стороне отношения (не {@code null})
     * @param <ID> Идентификатор левой стороны отношения
     * @param <F> Тип {@link Fetcher}-а
     * @param <R> Правая сторона отношения
     */
    public static <ID, F extends Fetcher<?, ?, ?>, R, C extends Collection<F>> Map<ID, C> joinOneToMany(
        Supplier<Collection<R>> innerJoinRightSide,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super R, ? extends ID> getLeftSideIdFromRightSide,
        Supplier<? extends C> targetCollectionSupplier
    ) {
        Map<ID, C> result = new HashMap<>();
        Collection<R> childEntities = innerJoinRightSide.get();
        for (R child : childEntities) {
            F fetcher = Objects.requireNonNull(fetcherConstructor.apply(child));
            ID leftSideId = Objects.requireNonNull(getLeftSideIdFromRightSide.apply(child), () -> "Missing left side for INNER join for " + child);
            result.computeIfAbsent(leftSideId, ignored -> targetCollectionSupplier.get()).add(fetcher);
        }
        return result;
    }

    public static <ID, F extends Fetcher<?, ?, ?>, R> Map<ID, List<F>> joinOneToMany(
        Supplier<Collection<R>> joinRightSide,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super R, ? extends ID> getLeftSideIdFromRightSide
    ) {
        return joinOneToMany(joinRightSide, fetcherConstructor, getLeftSideIdFromRightSide, ArrayList::new);
    }

    /**
     * Для общего типа отношений ToMany (unidirectional/bidirectional, OneToMany/ManyToMany).
     * Специфичен для JPA
     * (для OneToMany отношений лучше использовать {@link #joinOneToMany})
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
     * @param <L> Тип сущностей с левой стороны отношения
     * @param <R> Тип сущностей с правой стороны отношения
     * @param <C> Тип нужной коллекции
     */
    public static <ID, F extends Fetcher<?, ?, ?>, L, R, C extends Collection<F>> Map<ID, C> joinToMany(
        Supplier<Set<L>> innerJoinRightSide,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super L, ? extends ID> getLeftSideId,
        Function<? super L, Collection<R>> getRightSideByLeftSide,
        Supplier<? extends C> targetCollectionSupplier
    ) {
        Map<ID, C> result = new HashMap<>();
        Set<? extends L> joined = innerJoinRightSide.get();
        for (L leftSideEntity : joined) {
            ID leftSideId = Objects.requireNonNull(getLeftSideId.apply(leftSideEntity));
            Collection<? extends R> rightSide = getRightSideByLeftSide.apply(leftSideEntity);
            Assert.notEmpty(
                rightSide,
                () -> "Empty collection provided for INNER join. Left side entity: " + leftSideEntity + ", ID: " + leftSideId
            );
            checkDuplicates(leftSideId, leftSideEntity, result);
            C manySideFetchers = targetCollectionSupplier.get();
            result.put(leftSideId, manySideFetchers);
            for (R rightSideEntity : rightSide) {
                F fetcher = Objects.requireNonNull(fetcherConstructor.apply(rightSideEntity));
                manySideFetchers.add(fetcher);
            }
        }
        return result;
    }

    public static <ID, F extends Fetcher<?, ?, ?>, L, R> Map<ID, List<F>> joinToMany(
        Supplier<Set<L>> innerJoin,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super L, ? extends ID> getLeftSideId,
        Function<? super L, Collection<R>> getRightSideByLeftSide
    ) {
        return joinToMany(innerJoin, fetcherConstructor, getLeftSideId, getRightSideByLeftSide, ArrayList::new);
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
     * @param <LID> Идентификатор сущности с левой стороны отношения
     * @param <RID> Идентификатор сущности с правой стороны отношения
     * @param <F> Тип {@link Fetcher}
     * @param <L> Левая сторона отношения
     * @param <R> Правая сторона отношения
     */
    public static <LID, RID, F extends Fetcher<?, ?, ?>, L, R> Map<LID, F> joinToOne(
        Collection<L> leftSide,
        Supplier<Collection<R>> fetchRightSide,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super L, ? extends LID> getLeftSideId,
        Function<? super L, ? extends RID> getForeignKey,
        Function<? super R, ? extends RID> getRightSideId
    ) {
        Map<LID, F> result = new HashMap<>();
        Map<RID, R> joined = new HashMap<>();
        for (R rightSide : fetchRightSide.get()) {
            joined.putIfAbsent(getRightSideId.apply(rightSide), rightSide);
        }
        for (L owner : leftSide) {
            RID fk = getForeignKey.apply(owner);
            if (fk != null) {
                R r = joined.get(fk);
                LID leftSideId = Objects.requireNonNull(getLeftSideId.apply(owner));
                checkDuplicates(leftSideId, owner, result);
                Objects.requireNonNull(
                    r,
                    () -> "Relationship present on " + owner + " for right side via foreign key " + fk + ", but could not be found on joined right side"
                );
                F fetcher = Objects.requireNonNull(fetcherConstructor.apply(r));
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
     * @param <L> Владелец отношения
     * @param <R> Правая сторона отношения
     */
    public static <ID, F extends Fetcher<?, ?, ?>, L, R> Map<ID, F> joinToOnePrefetching(
        Collection<L> leftSide,
        Runnable prefetchRightSide,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super L, ? extends R> getOtherSideFromLeftSide,
        Function<? super L, ? extends ID> getLeftSideId
    ) {
       prefetchRightSide.run();
       Map<ID, F> result = new HashMap<>();
        for (L owner : leftSide) {
            R prefetchedOtherSide = getOtherSideFromLeftSide.apply(owner);
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
     * @param fetchRightSide Метод, делающий {@code join} с правой стороной отношения
     * @param fetcherConstructor Метод, возвращающий {@link Fetcher} {@code F} по экземпляру правой стороны отношения
     * @param getLeftSideIdFromRightSide Метод, возвращающий идентификатор левой стороны отношения по правой стороне отношения (или {@code null}, если отношения нет)
     * @param <ID> Идентификатор левой стороны отношения
     * @param <F> Тип {@link Fetcher}
     * @param <R> Правая сторона отношения
     */
    public static <ID, F extends Fetcher<?, ?, ?>, R> Map<ID, F> joinOneToOne(
        Supplier<Collection<R>> fetchRightSide,
        Function<? super R, ? extends F> fetcherConstructor,
        Function<? super R, ? extends ID> getLeftSideIdFromRightSide
    ) {
        Map<ID, F> result = new HashMap<>();
        Collection<R> owners = fetchRightSide.get();
        for (R owner : owners) {
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
