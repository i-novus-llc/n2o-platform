package net.n2oapp.platform.seek;

import com.google.common.base.Preconditions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    private final ConcurrentMap<String, ComparableExpressionBase<?>> resolvedProperties;
    private final String entityPrefix;
    private final EntityManager entityManager;
    private final NullabilityProvider nullabilityProvider;

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
            NullabilityProvided nullabilityProvided = repositoryInterface.getAnnotation(NullabilityProvided.class);
            Class<? extends NullabilityProvider> nullabilityProviderClass = nullabilityProvided == null ? DefaultNullabilityProvider.class : nullabilityProvided.by();
            this.nullabilityProvider = nullabilityProviderClass.getConstructor().newInstance();
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
            List<EnrichedSeekPivot> pivots = makeList(orders, copyPivots(criteria));
            ensureNoDuplicates(pivots);
            result = fetchWithSeekPredicate(pivots, orders, criteria.getSize() + 1, predicate);
            RequestedPageEnum page = criteria.getPage();
            if (page == NEXT) {
                hasNext = result.size() > criteria.getSize();
                if (hasNext)
                    result.remove(result.size() - 1);
                hasPrev = !new JPAQuery<>(entityManager).select(Expressions.ONE).from(path).where(inverseSeekPredicate(pivots, false), predicate).limit(1).fetch().isEmpty();
            } else if (page == PREV) {
                hasPrev = result.size() > criteria.getSize();
                Collections.reverse(result);
                if (hasPrev)
                    result.remove(0);
                hasNext = !new JPAQuery<>(entityManager).select(Expressions.ONE).from(path).where(inverseSeekPredicate(pivots, true), predicate).limit(1).fetch().isEmpty();
            } else {
                throw new IllegalStateException("Unexpected page enum: " + criteria.getPage());
            }
        }
        return SeekedPage.of(result, hasNext, hasPrev);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate inverseSeekPredicate(List<EnrichedSeekPivot> pivots, boolean reverse) {
        BooleanBuilder res = new BooleanBuilder();
        for (EnrichedSeekPivot pivot : pivots) {
            Order order = pivot.order;
            ComparableExpression<?> exp = pivot.asComparable;
            ComparableExpressionBase castedPivot = pivot.castedValue;
            if (castedPivot == null) {
                if (pivot.order.getNullHandling() == Sort.NullHandling.NULLS_FIRST) {
                    if (reverse)
                        res.and(pivot.property.isNotNull());
                    else
                        res.and(pivot.property.isNull());
                }
            } else {
                BooleanExpression base;
                if (order.isAscending()) {
                    base = exp.loe(castedPivot);
                } else {
                    base = exp.goe(castedPivot);
                }
                if (nullabilityProvider.nullable(pivot.property) && pivot.order.getNullHandling() == Sort.NullHandling.NULLS_LAST) {
                    if (reverse)
                        base = base.or(pivot.property.isNotNull());
                    else
                        base = base.or(pivot.property.isNull());
                }
                res.and(base);
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

    private List<EnrichedSeekPivot> makeList(List<Order> orders, List<SeekPivot> pivots) {
        List<EnrichedSeekPivot> res = new ArrayList<>(orders.size());
        for (Order order : orders) {
            ComparableExpressionBase<?> target = resolvedProperties.computeIfAbsent(order.getProperty(), this::findProperty);
            ComparableExpressionBase<?> cast;
            SeekPivot pivot = pivots.stream().filter(piv -> piv.getName().equals(order.getProperty())).findAny().orElse(null);
            if (pivot == null) {
                nullabilitySanityCheck(target, order.getProperty());
                cast = null;
            } else
                cast = cast(pivot.getName(), pivot.getLastValue(), target);
            EnrichedSeekPivot item = new EnrichedSeekPivot(order, cast, target);
            res.add(item);
        }
        return res;
    }

    private void checkCriteria(SeekableCriteria criteria) {
        Preconditions.checkArgument(criteria != null, "Criteria must not be null");
        Preconditions.checkArgument(criteria.getSize() > 0, "Criteria size must be > 0");
        Preconditions.checkArgument(criteria.getPage() != null, "A requested page must be specified");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(criteria.getOrders()), "Sorting must be applied");
    }

    private List<Order> copyOrders(SeekableCriteria criteria) {
        List<Order> result = new ArrayList<>(criteria.getOrders().size());
        for (Order order : criteria.getOrders()) {
            String property = order.getProperty().startsWith(entityPrefix) ? order.getProperty().substring(entityPrefix.length() + 1) : order.getProperty();
            if (criteria.getPage() == PREV || criteria.getPage() == LAST) {
                result.add(reverse(order, property));
            } else {
                result.add(new Order(order.getDirection(), property, getExplicitNullHandling(order, false)));
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
        return new Order(order.isAscending() ? DESC : ASC, property, getExplicitNullHandling(order, true));
    }

    @SuppressWarnings("unchecked")
    private Predicate seek(List<EnrichedSeekPivot> list) {
        BooleanBuilder res = new BooleanBuilder();
        for (int i = 0; i < list.size(); i++) {
            EnrichedSeekPivot next = list.get(i);
            if (next.castedValue == null && next.order.getNullHandling() == Sort.NullHandling.NULLS_LAST)
                continue;
            BooleanBuilder accum = new BooleanBuilder();
            for (int j = 0; j < i; j++) {
                EnrichedSeekPivot piv = list.get(j);
                if (piv.castedValue == null)
                    accum.and(piv.property.isNull());
                else {
                    accum.and(piv.property.eq(piv.castedValue));
                }
            }
            if (next.castedValue == null) {
                accum.and(next.property.isNotNull());
            } else {
                BooleanExpression base = compare(next);
                if (nullabilityProvider.nullable(next.property) && next.order.getNullHandling() == Sort.NullHandling.NULLS_LAST)
                    base = base.or(next.property.isNull());
                accum.and(base);
            }
            res.or(accum);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private BooleanExpression compare(
        EnrichedSeekPivot piv
    ) {
        if (piv.order.isAscending()) {
            return piv.asComparable.gt(piv.castedValue);
        } else {
            return piv.asComparable.lt(piv.castedValue);
        }
    }

    /**
     * Если null-handling не задан явно -- имитирует поведение PostgreSQL
     * (<a href="https://postgrespro.ru/docs/postgresql/13/queries-order">7.5. Сортировка строк</a>):
     * "По умолчанию значения NULL считаются больше любых других,
     * то есть подразумевается NULLS FIRST для порядка DESC и NULLS LAST в противном случае."
     */
    private Sort.NullHandling getExplicitNullHandling(Order order, boolean reverse) {
        Sort.NullHandling result;
        Sort.NullHandling provided = order.getNullHandling();
        if (provided != Sort.NullHandling.NATIVE) {
            result = provided;
        } else {
            if (order.isDescending())
                result = Sort.NullHandling.NULLS_FIRST;
            else
                result = Sort.NullHandling.NULLS_LAST;
        }
        if (reverse)
            result = result == Sort.NullHandling.NULLS_FIRST ? Sort.NullHandling.NULLS_LAST : Sort.NullHandling.NULLS_FIRST;
        return result;
    }

    private ComparableExpressionBase<?> cast(String name, String lastValue, ComparableExpressionBase<?> target) {
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

    private ComparableExpressionBase<?> findProperty(String property) {
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
                                return (ComparableExpressionBase<?>) o;
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

    private void nullabilitySanityCheck(ComparableExpressionBase<?> expression, String property) {
        Preconditions.checkState(nullabilityProvider.nullable(expression), "Property %s is null, but is not nullable according to nullability provider. Entity: %s", property, path.getMetadata().getName());
    }

    @SuppressWarnings("rawtypes")
    private static class EnrichedSeekPivot {

        private final Order order;
        private final ComparableExpressionBase castedValue;
        private final ComparableExpressionBase<?> property;
        private final ComparableExpression<?> asComparable;

        private EnrichedSeekPivot(Order order, ComparableExpressionBase<?> castedValue, ComparableExpressionBase<?> property) {
            this.order = order;
            this.castedValue = castedValue;
            this.property = property;
            this.asComparable = Expressions.asComparable(property);
        }

    }

}