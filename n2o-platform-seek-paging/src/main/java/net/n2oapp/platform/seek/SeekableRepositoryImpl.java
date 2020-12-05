package net.n2oapp.platform.seek;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
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
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.Order;
import static org.springframework.data.domain.Sort.by;

@NoRepositoryBean
public class SeekableRepositoryImpl<T> extends QuerydslJpaPredicateExecutor<T> implements SeekableRepository<T> {

    private final Querydsl querydsl;
    private final EntityPath<?> path;
    private final LoadingCache<String, ComparableExpression<?>> cache;

    public SeekableRepositoryImpl(
        JpaEntityInformation<T, ?> entityInformation,
        EntityManager entityManager,
        EntityPathResolver resolver,
        CrudMethodMetadata metadata
    )  {
        super(entityInformation, entityManager, resolver, metadata);
        try {
            Field querydslField = QuerydslJpaPredicateExecutor.class.getDeclaredField("querydsl");
            Field pathField = QuerydslJpaPredicateExecutor.class.getDeclaredField("path");
            querydslField.setAccessible(true);
            pathField.setAccessible(true);
            this.querydsl = (Querydsl) querydslField.get(this);
            this.path = (EntityPath<?>) pathField.get(this);
        } catch (Exception e) {
            throw new BeanCreationException("Can't instantiate seekable repository", e);
        }
        this.cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ComparableExpression<?> load(@NonNull String key) {
                return Expressions.asComparable(findProperty(key));
            }
        });
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
        List<T> fetch = fetch0(criteria, predicate, true);
        if (criteria.getPrev())
            Collections.reverse(fetch);
        boolean hasNext;
        boolean hasPrev;
        SeekableCriteria inverted = invert(criteria);
        if (criteria.getNext()) {
            hasNext = fetch.size() > criteria.getSize();
            if (hasNext)
                fetch.remove(fetch.size() - 1);
            hasPrev = !fetch0(inverted, predicate, false).isEmpty();
        } else {
            hasPrev = fetch.size() > criteria.getSize();
            if (hasPrev)
                fetch.remove(0);
            hasNext = !fetch0(inverted, predicate, false).isEmpty();
        }
        return SeekedPage.of(fetch, hasNext, hasPrev);
    }

    private SeekableCriteria invert(SeekableCriteria criteria) {
        SeekableCriteria inverted = new SeekableCriteria();
        inverted.setSize(0);
        inverted.setNext(!criteria.getNext());
        inverted.setPivots(criteria.getPivots());
        inverted.setOrders(criteria.getOrders());
        return inverted;
    }

    private List<T> fetch0(SeekableCriteria criteria, Predicate predicate, final boolean validate) {
        List<Order> orders = copyOrders(criteria);
        List<EnrichedSeekRequestItem> list = makeList(orders, copyPivots(criteria));
        if (validate)
            ensureNoDuplicates(list);
        Predicate seekPredicate = seek(list);
        JPQLQuery<?> query = getQuery(predicate, seekPredicate);
        return fetch(query, criteria.getSize(), orders);
    }

    private JPQLQuery<?> getQuery(Predicate predicate, Predicate seekPredicate) {
        return predicate == null ? super.createQuery(seekPredicate) : super.createQuery(predicate, seekPredicate);
    }

    private List<EnrichedSeekRequestItem> makeList(List<Order> orders, List<SeekPivot> pivots) {
        List<EnrichedSeekRequestItem> res = new ArrayList<>(orders.size());
        for (Order order : orders) {
            SeekPivot pivot = pivots.stream().filter(piv -> piv.getName().equals(order.getProperty())).findAny().orElseThrow(() -> {
                throw new IllegalArgumentException(order.getProperty() + " is not found in pivots list.");
            });
            ComparableExpression<?> comparable = cache.getUnchecked(pivot.getName());
            Comparable<?> cast = cast(pivot.getLastValue(), comparable);
            EnrichedSeekRequestItem item = new EnrichedSeekRequestItem(cast, order, comparable);
            res.add(item);
        }
        return res;
    }

    private void checkCriteria(SeekableCriteria criteria) {
        Preconditions.checkArgument(criteria != null, "Criteria must not be null");
        Preconditions.checkArgument(criteria.getSize() >= 0, "Criteria size must be >= 0");
        Preconditions.checkArgument(criteria.getPrev() != criteria.getNext(), "Either prev page or next page must be specified (but not both)");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(criteria.getOrders()), "Sorting must be applied");
        Preconditions.checkArgument(!CollectionUtils.isEmpty(criteria.getPivots()), "Pivots (last seen values) must be specified");
        Preconditions.checkArgument(criteria.getOrders().size() == criteria.getPivots().size(), "Num of pivots and sorting fields must be equal");
    }

    private List<Order> copyOrders(SeekableCriteria criteria) {
        List<Order> result = new ArrayList<>(criteria.getOrders().size());
        for (Order order : criteria.getOrders()) {
            if (criteria.getNext()) {
                result.add(new Order(order.getDirection(), order.getProperty(), order.getNullHandling()));
            } else {
                result.add(new Order(order.getDirection() == ASC ? DESC : ASC, order.getProperty(), order.getNullHandling()));
            }
        }
        return result;
    }

    private List<SeekPivot> copyPivots(SeekableCriteria criteria) {
        List<SeekPivot> pivots = new ArrayList<>(criteria.getPivots().size());
        for (SeekPivot pivot : criteria.getPivots()) {
            pivots.add(pivot.copy());
        }
        return pivots;
    }

    private void ensureNoDuplicates(List<EnrichedSeekRequestItem> list) {
        for (int i = 0; i < list.size(); i++) {
            EnrichedSeekRequestItem item1 = list.get(i);
            for (int j = 0; j < i; j++) {
                EnrichedSeekRequestItem item2 = list.get(j);
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
        query = limitAndSort(query, limit, orders);
        return (List<T>) query.fetch();
    }

    private JPQLQuery<?> limitAndSort(JPQLQuery<?> query, long limit, List<Order> list) {
        query = query.limit(limit + 1L);
        query = querydsl.applySorting(by(list), query);
        return query;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate seek(List<EnrichedSeekRequestItem> list) {
        BooleanExpression res = null;
        for (int i = 0; i < list.size(); i++) {
            EnrichedSeekRequestItem item = list.get(i);
            Order order = item.order;
            ComparableExpression exp = item.comparable;
            Comparable<?> cast = item.castedPivot;
            if (res == null) {
                res = order.getDirection() == ASC ? exp.gt(cast) : exp.lt(cast);
            } else {
                BooleanExpression temp = null;
                for (int j = 0; j < i; j++) {
                    EnrichedSeekRequestItem tempItem = list.get(j);
                    ComparableExpression tempExp = tempItem.comparable;
                    Comparable<?> tempCast = tempItem.castedPivot;
                    if (temp == null) {
                        temp = tempExp.eq(tempCast);
                    } else {
                        temp = temp.and(tempExp.eq(tempCast));
                    }
                }
                temp = order.getDirection() == ASC ? temp.and(exp.gt(cast)) : temp.and(exp.lt(cast));
                res = res.or(temp);
            }
        }
        return res;
    }

    private Comparable<?> cast(String lastValue, ComparableExpression<?> target) {
        try {
            return StringConvert.INSTANCE.convertFromString(target.getType(), lastValue);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                "Unable to convert from string '" + lastValue + "' to target type: " + target.getType().getName() + ". " +
                "Entity name: " + path.getMetadata().getName()
            );
        }
    }

    private ComparableExpressionBase<?> findProperty(String name) {
        Path<?> curr = path;
        do {
            Class<?> c = curr.getClass();
            Field superField = null;
            try {
                for (Field field : c.getDeclaredFields()) {
                    if (field.getName().equals(name)) {
                        field.setAccessible(true);
                        Object o = field.get(curr);
                        Preconditions.checkArgument(o != null, "Path is null. Entity: %s", curr.getMetadata().getName());
                        Preconditions.checkArgument(ComparableExpressionBase.class.isAssignableFrom(o.getClass()), "Property %s is not comparable. Entity: %s", name, curr.getMetadata().getName());
                        return ((ComparableExpressionBase<?>) o);
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
        throw new IllegalArgumentException("Property " + name + " not found. Entity: " + path.getMetadata().getName());
    }

    private static class EnrichedSeekRequestItem {

        private final Comparable<?> castedPivot;
        private final Order order;
        private final ComparableExpression comparable;

        private EnrichedSeekRequestItem(Comparable<?> castedPivot, Order order, ComparableExpression comparable) {
            this.castedPivot = castedPivot;
            this.order = order;
            this.comparable = comparable;
        }

    }

}
