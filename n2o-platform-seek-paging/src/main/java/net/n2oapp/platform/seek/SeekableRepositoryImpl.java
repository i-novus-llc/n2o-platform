package net.n2oapp.platform.seek;

import com.google.common.base.Preconditions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import net.n2oapp.platform.jaxrs.seek.RequestedPageEnum;
import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekableCriteria;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.joda.convert.StringConvert;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.emptyList;
import static net.n2oapp.platform.jaxrs.seek.RequestedPageEnum.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.Order;
import static org.springframework.data.domain.Sort.by;

@NoRepositoryBean
public class SeekableRepositoryImpl<T> extends QuerydslJpaPredicateExecutor<T> implements SeekableRepository<T> {

    private final Querydsl querydsl;
    private final EntityPath<?> path;
    private final ConcurrentMap<String, ComparableExpression<?>> resolvedProperties;
    private final String entityPrefix;
    private final PivotProvider provider;
    private final EntityManager entityManager;

    public SeekableRepositoryImpl(
        JpaEntityInformation<T, ?> entityInformation,
        EntityManager entityManager,
        EntityPathResolver resolver,
        CrudMethodMetadata metadata,
        Class<?> repositoryInterface
    )  {
        super(entityInformation, entityManager, resolver, metadata);
        try {
            Field querydslField = QuerydslJpaPredicateExecutor.class.getDeclaredField("querydsl");
            Field pathField = QuerydslJpaPredicateExecutor.class.getDeclaredField("path");
            querydslField.setAccessible(true);
            pathField.setAccessible(true);
            this.querydsl = (Querydsl) querydslField.get(this);
            this.path = (EntityPath<?>) pathField.get(this);
            PivotProvided ann = repositoryInterface.getAnnotation(PivotProvided.class);
            if (ann != null)
                this.provider = ann.by().getConstructor().newInstance();
            else
                this.provider = new DefaultPivotProvider();
        } catch (Exception e) {
            throw new BeanCreationException("Can't instantiate seekable repository", e);
        }
        this.entityPrefix = resolver.createPath(entityInformation.getJavaType()).getMetadata().getName();
        this.resolvedProperties = new ConcurrentHashMap<>();
        this.entityManager = entityManager;
    }

    @Override
    public SeekedPage<T> findAll(SeekableCriteria criteria) {
        return fetch(criteria, null);
    }

    @Override
    public SeekedPage<T> findAll(SeekableCriteria criteria, Predicate predicate) {
        return fetch(criteria, predicate);
    }

    private SeekedPage<T> fetch(SeekableCriteria criteria, Predicate predicate) {
        List<T> result;
        checkCriteria(criteria);
        List<Order> orders = copyOrders(criteria);
        boolean hasNext;
        boolean hasPrev;
        if (criteria.getPage() == FIRST || criteria.getPage() == LAST) {
            result = fetchSimple(orders, predicate, criteria.getSize() + 1);
            hasNext = criteria.getPage() == FIRST && result.size() > criteria.getSize();
            hasPrev = criteria.getPage() == LAST && result.size() > criteria.getSize();
            if (criteria.getPage() == LAST) {
                Collections.reverse(result);
                if (hasPrev) {
                    result.remove(0);
                }
            } else {
                if (hasNext)
                    result.remove(result.size() - 1);
            }
        } else {
            List<EnrichedSeekPivot> pivots = makeList(criteria, orders, copyPivots(criteria));
            ensureNoDuplicates(pivots);
            result = fetchWithSeekPredicate(pivots, orders, criteria.getSize() + 1, predicate);
            RequestedPageEnum page = criteria.getPage();
            if (page == NEXT) {
                hasNext = result.size() > criteria.getSize();
                if (hasNext)
                    result.remove(result.size() - 1);
                hasPrev = !new JPAQuery<>(entityManager).select(Expressions.ONE).from(path).where(inverseSeekPredicate(pivots), predicate).limit(1).fetch().isEmpty();
            } else if (page == PREV) {
                hasPrev = result.size() > criteria.getSize();
                Collections.reverse(result);
                if (hasPrev)
                    result.remove(0);
                hasNext = !new JPAQuery<>(entityManager).select(Expressions.ONE).from(path).where(inverseSeekPredicate(pivots), predicate).limit(1).fetch().isEmpty();
            } else {
                throw new IllegalStateException("Unexpected page enum: " + criteria.getPage());
            }
        }
        return SeekedPage.of(result, hasNext, hasPrev);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate inverseSeekPredicate(List<EnrichedSeekPivot> pivots) {
        BooleanBuilder res = new BooleanBuilder();
        for (EnrichedSeekPivot pivot : pivots) {
            Order order = pivot.order;
            ComparableExpression exp = pivot.comparable;
            ComparableExpressionBase castedPivot = pivot.castedPivot;
            if (order.isAscending()) {
                res.and(exp.loe(castedPivot));
            } else {
                res.and(exp.goe(castedPivot));
            }
        }
        return res;
    }

    private List<T> fetchSimple(List<Order> orders, Predicate predicate, int size) {
        JPQLQuery<?> query = getQuery(predicate, Expressions.ONE.eq(Expressions.ONE));
        return fetch(query, size, orders);
    }

    private List<T> fetchWithSeekPredicate(
            List<EnrichedSeekPivot> list,
            List<Order> orders,
            int size,
            final Predicate predicate
    ) {
        Predicate seekPredicate = seek(list);
        JPQLQuery<?> query = getQuery(predicate, seekPredicate);
        return fetch(query, size, orders);
    }

    private JPQLQuery<?> getQuery(Predicate predicate, Predicate seekPredicate) {
        return predicate == null ? super.createQuery(seekPredicate) : super.createQuery(predicate, seekPredicate);
    }

    private List<EnrichedSeekPivot> makeList(SeekableCriteria criteria, List<Order> orders, List<SeekPivot> pivots) {
        List<EnrichedSeekPivot> res = new ArrayList<>(orders.size());
        for (Order order : orders) {
            ComparableExpression<?> target = resolvedProperties.computeIfAbsent(order.getProperty(), this::findProperty);
            ComparableExpressionBase<?> cast;
            if (criteria.getPage().isPivotsNecessary()) {
                SeekPivot pivot = pivots.stream().filter(piv -> piv.getName().equals(order.getProperty())).findAny().orElseThrow(() -> {
                    throw new IllegalArgumentException(order.getProperty() + " is not found in pivots list.");
                });
                cast = cast(pivot.getName(), pivot.getLastValue(), target);
            } else {
                cast = getFromPivotProvider(order, target);
            }
            EnrichedSeekPivot item = new EnrichedSeekPivot(cast, order, target);
            res.add(item);
        }
        return res;
    }

    private ComparableExpressionBase<?> getFromPivotProvider(Order order, ComparableExpression<?> target) {
        ComparableExpressionBase<?> cast;
        String min = "Min";
        String max = "Max";
        String description;
        if (order.isAscending()) {
            cast = provider.min(target);
            description = min;
        } else {
            cast = provider.max(target);
            description = max;
        }
        Preconditions.checkNotNull(cast, "%s value for property %s is not configured. Entity name: %s", description, order.getProperty(), path.getMetadata().getName());
        return cast;
    }

    private void checkCriteria(SeekableCriteria criteria) {
        Preconditions.checkArgument(criteria != null, "Criteria must not be null");
        Preconditions.checkArgument(criteria.getSize() > 0, "Criteria size must be > 0");
        Preconditions.checkArgument(criteria.getPage() != null, "Requested page must be specified");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(criteria.getOrders()), "Sorting must be applied");
        if (criteria.getPage().isPivotsNecessary()) {
            Preconditions.checkArgument(!CollectionUtils.isEmpty(criteria.getPivots()), "Pivots (last seen values) must be specified");
            Preconditions.checkArgument(criteria.getOrders().size() == criteria.getPivots().size(), "Num of pivots and sorting fields must be equal");
        }
    }

    private List<Order> copyOrders(SeekableCriteria criteria) {
        List<Order> result = new ArrayList<>(criteria.getOrders().size());
        for (Order order : criteria.getOrders()) {
            String property = order.getProperty().startsWith(entityPrefix) ? order.getProperty().substring(entityPrefix.length() + 1) : order.getProperty();
            if (criteria.getPage() == PREV || criteria.getPage() == LAST) {
                result.add(reverse(order, property));
            } else {
                result.add(new Order(order.getDirection(), property, order.getNullHandling()));
            }
        }
        return result;
    }

    private List<SeekPivot> copyPivots(SeekableCriteria criteria) {
        if (criteria.getPivots() == null)
            return emptyList();
        List<SeekPivot> pivots = new ArrayList<>(criteria.getPivots().size());
        for (SeekPivot pivot : criteria.getPivots()) {
            if (!pivot.getName().startsWith(entityPrefix))
                pivots.add(pivot.copy());
            else
                pivots.add(SeekPivot.of(pivot.getName().substring(entityPrefix.length() + 1), pivot.getLastValue()));
        }
        return pivots;
    }

    private void ensureNoDuplicates(List<EnrichedSeekPivot> list) {
        for (int i = 0; i < list.size(); i++) {
            EnrichedSeekPivot item1 = list.get(i);
            for (int j = 0; j < i; j++) {
                EnrichedSeekPivot item2 = list.get(j);
                Preconditions.checkArgument(
                    !item1.order.getProperty().equals(item2.order.getProperty()),
                    "%s duplicated in criteria",
                    item1.order.getProperty()
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> fetch(JPQLQuery<?> query, long limit, List<Order> orders) {
        query = query.limit(limit);
        query = querydsl.applySorting(by(orders), query);
        return (List<T>) query.fetch();
    }

    private Order reverse(Order order, String property) {
        return new Order(order.isAscending() ? DESC : ASC, property, order.getNullHandling());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate seek(List<EnrichedSeekPivot> list) {
        BooleanBuilder res = new BooleanBuilder();
        for (int i = 0; i < list.size(); i++) {
            EnrichedSeekPivot next = list.get(i);
            Order order = next.order;
            ComparableExpression exp = next.comparable;
            ComparableExpressionBase<?> cast = next.castedPivot;
            BooleanBuilder accum = new BooleanBuilder();
            for (int j = 0; j < i; j++) {
                EnrichedSeekPivot piv = list.get(j);
                accum.and(piv.comparable.eq(piv.castedPivot));
            }
            compare(accum, exp, cast, order);
            res.or(accum);
        }
        return res;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void compare(
            BooleanBuilder accum,
            ComparableExpression exp,
            ComparableExpressionBase castedPivot,
            Order order
    ) {
        if (order.isAscending()) {
            accum.and(exp.gt(castedPivot));
        } else {
            accum.and(exp.lt(castedPivot));
        }
    }

    /**
     * Имитирует поведение PostgreSQL (<a href="https://postgrespro.ru/docs/postgresql/13/queries-order">7.5. Сортировка строк</a>):
     * "По умолчанию значения NULL считаются больше любых других,
     * то есть подразумевается NULLS FIRST для порядка DESC и NULLS LAST в противном случае."
     */
    private Sort.NullHandling getNullHandling(Sort.Order order) {
        Sort.NullHandling nullHandling = order.getNullHandling();
        if (nullHandling != Sort.NullHandling.NATIVE)
            return nullHandling;
        if (order.isAscending())
            return Sort.NullHandling.NULLS_LAST;
        return Sort.NullHandling.NULLS_FIRST;
    }

    private ComparableExpressionBase<?> cast(String name, String lastValue, ComparableExpression<?> target) {
        Comparable<?> casted;
        try {
            casted = StringConvert.INSTANCE.convertFromString(target.getType(), lastValue);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                "Unable to convert from string '" + lastValue + "' to target type: " + target.getType().getSimpleName() + ". " +
                "Entity name: " + path.getMetadata().getName() +
                "Property: " + name,
                exception
            );
        }
        return Expressions.asComparable(casted);
    }

    private ComparableExpression<?> findProperty(String property) {
        Path<?> curr = path;
        String[] pathParts = property.split("\\.");
        for (int i = 0; i < pathParts.length; i++) {
            String name = pathParts[i];
            outer: do {
                Class<?> c = curr.getClass();
                Field superField = null;
                try {
                    for (Field field : c.getDeclaredFields()) {
                        if (field.getName().equals(name)) {
                            field.setAccessible(true);
                            Object o = field.get(curr);
                            Preconditions.checkArgument(o != null, "Path (or part of the path) %s is null. Entity: %s", property, curr.getMetadata().getName());
                            if (i == pathParts.length - 1) {
                                Preconditions.checkArgument(ComparableExpressionBase.class.isAssignableFrom(o.getClass()), "Property %s is not comparable. Entity: %s", property, curr.getMetadata().getName());
                                return Expressions.asComparable(((ComparableExpressionBase<?>) o));
                            } else {
                                curr = (Path<?>) o;
                                break outer;
                            }
                        }
                        if (field.getName().equals("_super"))
                            superField = field;
                    }
                    if (superField == null)
                        break;
                    superField.setAccessible(true);
                    curr = (Path<?>) superField.get(curr);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            } while (true);
        }
        throw new IllegalArgumentException("Property " + property + " not found. Entity: " + path.getMetadata().getName());
    }

    @SuppressWarnings("rawtypes")
    private static class EnrichedSeekPivot {

        private final ComparableExpressionBase castedPivot;
        private final Order order;
        private final ComparableExpression comparable;

        private EnrichedSeekPivot(ComparableExpressionBase<?> castedPivot, Order order, ComparableExpression<?> comparable) {
            this.castedPivot = castedPivot;
            this.order = order;
            this.comparable = comparable;
        }

    }

}
