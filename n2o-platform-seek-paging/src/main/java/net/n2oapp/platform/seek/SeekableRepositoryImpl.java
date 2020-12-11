package net.n2oapp.platform.seek;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import net.n2oapp.platform.jaxrs.seek.RequestedPageEnum;
import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekableCriteria;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.joda.convert.StringConvert;
import org.springframework.beans.factory.BeanCreationException;
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

import static java.util.stream.Collectors.toList;
import static net.n2oapp.platform.jaxrs.seek.RequestedPageEnum.LAST;
import static net.n2oapp.platform.jaxrs.seek.RequestedPageEnum.PREV;
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
        checkCriteria(criteria);
        List<Order> orders = copyOrders(criteria);
        List<EnrichedSeekPivot> list = makeList(criteria, orders, copyPivots(criteria));
        ensureNoDuplicates(list);
        List<T> fetch = fetch0(list, orders, criteria.getSize() + 1, predicate, false);
        if (criteria.getPage() == PREV || criteria.getPage() == LAST)
            Collections.reverse(fetch);
        RequestedPageEnum savedPage = criteria.getPage();
        int savedSize = criteria.getSize();
        boolean hasNext;
        boolean hasPrev;
        try {
            switch (criteria.getPage()) {
                case NEXT:
                    hasNext = fetch.size() > savedSize;
                    if (hasNext)
                        fetch.remove(fetch.size() - 1);
                    hasPrev = !fetch0(list, orders, 1, predicate, true).isEmpty();
                    break;
                case PREV:
                    hasPrev = fetch.size() > savedSize;
                    if (hasPrev)
                        fetch.remove(0);
                    hasNext = !fetch0(list, orders, 1, predicate, true).isEmpty();
                    break;
                case FIRST:
                    hasPrev = false;
                    hasNext = fetch.size() > savedSize;
                    if (hasNext)
                        fetch.remove(fetch.size() - 1);
                    break;
                case LAST:
                    hasNext = false;
                    hasPrev = fetch.size() > savedSize;
                    if (hasPrev)
                        fetch.remove(0);
                    break;
                default:
                    throw new IllegalStateException("Unexpected requested page: " + criteria.getPage());
            }
        } finally {
            criteria.setPage(savedPage);
            criteria.setSize(savedSize);
        }
        return SeekedPage.of(fetch, hasNext, hasPrev);
    }

    private List<T> fetch0(List<EnrichedSeekPivot> list, List<Order> orders, int size, final Predicate predicate, final boolean reverse) {
        Predicate seekPredicate = seek(list, reverse);
        JPQLQuery<?> query = getQuery(predicate, seekPredicate);
        return fetch(query, size, orders, reverse);
    }

    private JPQLQuery<?> getQuery(Predicate predicate, Predicate seekPredicate) {
        return predicate == null ? super.createQuery(seekPredicate) : super.createQuery(predicate, seekPredicate);
    }

    private List<EnrichedSeekPivot> makeList(SeekableCriteria criteria, List<Order> orders, List<SeekPivot> pivots) {
        List<EnrichedSeekPivot> res = new ArrayList<>(orders.size());
        for (Order order : orders) {
            ComparableExpression<?> comparable = resolvedProperties.computeIfAbsent(order.getProperty(), this::findProperty);
            Comparable<?> cast;
            if (criteria.getPage().isPivotsNecessary()) {
                SeekPivot pivot = pivots.stream().filter(piv -> piv.getName().equals(order.getProperty())).findAny().orElseThrow(() -> {
                    throw new IllegalArgumentException(order.getProperty() + " is not found in pivots list.");
                });
                cast = cast(pivot.getName(), pivot.getLastValue(), comparable);
            } else {
                String min = "Min";
                String max = "Max";
                String description;
                if (order.isAscending()) {
                    cast = provider.min(comparable);
                    description = min;
                } else {
                    cast = provider.max(comparable);
                    description = max;
                }
                Preconditions.checkNotNull(cast, "%s value for property %s is not configured. Entity name: %s", description, order.getProperty(), path.getMetadata().getName());
            }
            EnrichedSeekPivot item = new EnrichedSeekPivot(cast, order, comparable);
            res.add(item);
        }
        return res;
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
    private List<T> fetch(JPQLQuery<?> query, long limit, List<Order> orders, boolean reverse) {
        query = query.limit(limit);
        if (reverse)
            orders = orders.stream().map(this::reverse).collect(toList());
        query = querydsl.applySorting(by(orders), query);
        return (List<T>) query.fetch();
    }

    private Order reverse(Order order) {
        return reverse(order, order.getProperty());
    }

    private Order reverse(Order order, String property) {
        return new Order(order.isAscending() ? DESC : ASC, property, order.getNullHandling());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate seek(List<EnrichedSeekPivot> list, boolean reverse) {
        BooleanExpression res = null;
        for (int i = 0; i < list.size(); i++) {
            EnrichedSeekPivot next = list.get(i);
            Order order = next.order;
            ComparableExpression exp = next.comparable;
            Comparable<?> cast = next.castedPivot;
            if (res == null) {
                res = compare(exp.lt(cast), exp.gt(cast), order, reverse);
            } else {
                BooleanExpression accum = equalize(null, list.get(0));
                for (int j = 1; j < i; j++) {
                    accum = equalize(accum, list.get(j));
                }
                accum = compare(accum.and(exp.lt(cast)), accum.and(exp.gt(cast)), order, reverse);
                res = res.or(accum);
            }
        }
        return res;
    }

    private BooleanExpression compare(
        BooleanExpression lessThan,
        BooleanExpression greaterThan,
        Order order,
        boolean reverse
    ) {
        if (reverse)
            return order.isAscending() ? lessThan : greaterThan;
        else
            return order.isAscending() ? greaterThan : lessThan;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BooleanExpression equalize(BooleanExpression accum, EnrichedSeekPivot piv) {
        ComparableExpression exp = piv.comparable;
        Comparable<?> cast = piv.castedPivot;
        if (accum == null) {
            accum = exp.eq(cast);
        } else {
            accum = accum.and(exp.eq(cast));
        }
        return accum;
    }

    private Comparable<?> cast(String name, String lastValue, ComparableExpression<?> target) {
        try {
            return StringConvert.INSTANCE.convertFromString(target.getType(), lastValue);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                "Unable to convert from string '" + lastValue + "' to target type: " + target.getType().getSimpleName() + ". " +
                "Entity name: " + path.getMetadata().getName() +
                "Property: " + name,
                exception
            );
        }
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

    private static class EnrichedSeekPivot {

        private final Comparable<?> castedPivot;
        private final Order order;
        private final ComparableExpression comparable;

        private EnrichedSeekPivot(Comparable<?> castedPivot, Order order, ComparableExpression comparable) {
            this.castedPivot = castedPivot;
            this.order = order;
            this.comparable = comparable;
        }

    }

}
