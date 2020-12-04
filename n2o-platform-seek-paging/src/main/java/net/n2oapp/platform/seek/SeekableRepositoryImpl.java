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
import java.util.Iterator;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.Order;
import static org.springframework.data.domain.Sort.by;

@NoRepositoryBean
public class SeekableRepositoryImpl<T> extends QuerydslJpaPredicateExecutor<T> implements SeekableRepository<T> {

    private final Querydsl querydsl;
    private final EntityPath<?> path;

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
    }

    @Override
    public List<T> findAll(SeekableCriteria criteria) {
        return fetch(criteria, null);
    }

    @Override
    public List<T> findAll(SeekableCriteria criteria, Predicate predicate) {
        return fetch(criteria, predicate);
    }

    private List<T> fetch(SeekableCriteria criteria, Predicate predicate) {
        checkCriteria(criteria);
        List<Order> orders = copyOrders(criteria);
        sort(orders, criteria);
        Predicate seekPredicate = seek(criteria, orders);
        JPQLQuery<?> query = predicate == null ? super.createQuery(seekPredicate) : super.createQuery(predicate, seekPredicate);
        return fetch(query, criteria, orders);
    }

    private void checkCriteria(SeekableCriteria criteria) {
        Preconditions.checkArgument(criteria != null, "Criteria must not be null");
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

    private void sort(List<Order> orders, SeekableCriteria criteria) {
        orders.sort(((o1, o2) -> {
            int i = -1;
            int j = -1;
            int idx = 0;
            for (SeekPivot pivot : criteria.getPivots()) {
                if (i == -1 && pivot.getName().equals(o1.getProperty())) {
                    i = idx;
                }
                if (j == -1 && pivot.getName().equals(o2.getProperty())) {
                    j = idx;
                }
                idx++;
            }
            Preconditions.checkArgument(i != -1, "%s not found in pivots list", o1.getProperty());
            Preconditions.checkArgument(j != -1, "%s not found in pivots list", o2.getProperty());
            Preconditions.checkArgument(i != j, "%s duplicated in pivots list", o1.getProperty());
            return Integer.compare(i, j);
        }));
    }

    @SuppressWarnings("unchecked")
    private List<T> fetch(JPQLQuery<?> query, SeekableCriteria criteria, List<Order> orders) {
        query = limitAndSort(query, criteria, orders);
        List<T> fetch = (List<T>) query.fetch();
        if (criteria.getNext())
            return fetch;
        Collections.reverse(fetch);
        return fetch;
    }

    private JPQLQuery<?> limitAndSort(JPQLQuery<?> query, SeekableCriteria criteria, List<Order> orders) {
        query = query.limit(criteria.getSize());
        query = querydsl.applySorting(by(orders), query);
        return query;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate seek(SeekableCriteria criteria, List<Order> orders) {
        BooleanBuilder builder = new BooleanBuilder();
        if (uniformSorting(orders)) {
            Iterator<SeekPivot> pivIterator = criteria.getPivots().iterator();
            Iterator<Order> orderIterator = orders.iterator();
            while (pivIterator.hasNext()) {
                SeekPivot pivot = pivIterator.next();
                Order order = orderIterator.next();
                ComparableExpression exp = Expressions.asComparable(findProperty(pivot.getName()));
                Comparable<?> cast = cast(pivot.getLastValue(), exp);
                if (order.getDirection() == DESC)
                    builder.and(exp.lt(Expressions.asComparable(cast)));
                else
                    builder.and(exp.gt(Expressions.asComparable(cast)));
            }
        } else {

        }
        return builder.getValue();
    }

    private Comparable<?> cast(String lastValue, ComparableExpression<?> target) {
        return StringConvert.INSTANCE.convertFromString(target.getType(), lastValue);
    }

    private boolean uniformSorting(List<Order> orders) {
        boolean asc = false;
        boolean desc = false;
        for (Order order : orders) {
            asc |= order.getDirection() == ASC;
            desc |= order.getDirection() == DESC;
        }
        return asc ^ desc;
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
                throw new IllegalArgumentException(e);
            }
        } while (true);
        throw new IllegalArgumentException("Property " + name + " not found. Entity: " + path.getMetadata().getName());
    }

}
