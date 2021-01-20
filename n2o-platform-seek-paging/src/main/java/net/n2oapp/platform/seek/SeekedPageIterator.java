package net.n2oapp.platform.seek;

import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.base.Preconditions;
import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekableCriteria;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.n2oapp.platform.jaxrs.seek.RequestedPageEnum.*;

/**
 * Итератор по страницам {@link SeekedPage}.
 *
 * @param <T> Тип элементов
 * @param <C> Тип критерии
 */
public class SeekedPageIterator<T, C extends SeekableCriteria> implements Iterator<SeekedPage<T>> {

    private final Function<? super C, SeekedPage<T>> pageSource;
    private final BiFunction<? super T, ? super List<Sort.Order>, List<SeekPivot>> pivotsMaker;
    private final C criteria;

    private SeekedPage<T> next;
    private boolean hasNextCalled;
    private final boolean forward;

    private SeekedPageIterator(
        Function<? super C, SeekedPage<T>> pageSource,
        BiFunction<? super T, ? super List<Sort.Order>, List<SeekPivot>> pivotsMaker,
        C criteria
    ) {
        this.pageSource = pageSource;
        this.pivotsMaker = pivotsMaker;
        this.criteria = criteria;
        this.forward = criteria.getPage() == NEXT || criteria.getPage() == FIRST;
    }

    @Override
    public boolean hasNext() {
        if (hasNextCalled)
            return next != null;
        next0();
        hasNextCalled = true;
        return next != null;
    }

    @Override
    public SeekedPage<T> next() {
        SeekedPage<T> res;
        if (hasNextCalled) {
            res = this.next;
            hasNextCalled = false;
            next = null;
        } else {
            next0();
            res = next;
        }
        if (res == null)
            throw new NoSuchElementException();
        return res;
    }

    private void next0() {
        next = pageSource.apply(criteria);
        List<T> content = next.getContent();
        if (!content.isEmpty()) {
            if (forward) {
                criteria.setPivots(pivotsMaker.apply(content.get(content.size() - 1), criteria.getOrders()));
                if (criteria.getPage() == FIRST)
                    criteria.setPage(NEXT);
            } else {
                criteria.setPivots(pivotsMaker.apply(content.get(0), criteria.getOrders()));
                if (criteria.getPage() == LAST)
                    criteria.setPage(PREV);
            }
        } else {
            next = null;
        }
    }

    /**
     * @param pageSource Источник данных
     * @param criteria Критерия (будет модифицирована)
     * @param <T> Тип элементов
     * @param <C> Тип критерии
     * @return  Итератор, который будет использовать {@link ReflectionPivotsMaker#INSTANCE} в качестве {@code pivotsMaker}-а.
     *          Это удобно, но медленно, негибко и не всегда подходит.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, C extends SeekableCriteria> SeekedPageIterator<T, C> from(
        Function<? super C, SeekedPage<T>> pageSource,
        C criteria
    ) {
        return from(pageSource, ((ReflectionPivotsMaker) ReflectionPivotsMaker.INSTANCE), criteria);
    }

    /**
     * Метод для ситуаций, когда вызывающий не знает точно, какой порядок задан в передаваемой {@code criteria}.
     *
     * @param pageSource Источник данных
     * @param pivotsMaker Функция, принимающая элемент типа {@code T} и список {@link Sort.Order} {@code orders}
     *                    и возвращающая список {@link SeekPivot}-ов в соответствии со списком {@code orders}.
     * @param criteria Критерия (будет модифицирована)
     * @param <T> Тип элементов
     * @param <C> Тип критерии
     */
    public static <T, C extends SeekableCriteria> SeekedPageIterator<T, C> from(
        Function<? super C, SeekedPage<T>> pageSource,
        BiFunction<? super T, ? super List<Sort.Order>, List<SeekPivot>> pivotsMaker,
        C criteria
    ) {
        return new SeekedPageIterator<>(pageSource, pivotsMaker, criteria);
    }

    /**
     * Метод для ситуаций, когда тот, кто создал итератор, задал список {@link SeekableCriteria#getOrders()}.
     * То есть вызывающий точно знает, какая сортировка будет и может передать эффективную реализацию {@code pivotsMaker}.
     *
     * @param pageSource Источник данных
     * @param pivotsMaker Функция, принимающая элемент типа {@code T} и
     *                    возвращающая список {@link SeekPivot}-ов в
     *                    соответствии со списком {@link SeekableCriteria#getOrders()} в переданной критерии
     * @param criteria Критерия (будет модифицирована)
     * @param <T> Тип элементов
     * @param <C> Тип критерии
     */
    public static <T, C extends SeekableCriteria> SeekedPageIterator<T, C> from(
        Function<? super C, SeekedPage<T>> pageSource,
        Function<? super T, List<SeekPivot>> pivotsMaker,
        C criteria
    ) {
        return from(pageSource, (t, unused) -> pivotsMaker.apply(t), criteria);
    }

    public static class ReflectionPivotsMaker<T> implements BiFunction<T, List<Sort.Order>, List<SeekPivot>> {

        static final ReflectionPivotsMaker<?> INSTANCE = new ReflectionPivotsMaker<>();

        protected ReflectionPivotsMaker() {
        }

        @Override
        public List<SeekPivot> apply(T t, List<Sort.Order> orders) {
            List<SeekPivot> res = new ArrayList<>();
            for (Sort.Order order : orders) {
                String property = order.getProperty();
                Iterator<String> tokensIter = new Iterator<>() {

                    int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < property.length();
                    }

                    @Override
                    @SuppressWarnings("java:S2272")
                    public String next() {
                        int next = property.indexOf('.', this.i);
                        if (next == -1) {
                            String res = property.substring(i);
                            i = property.length();
                            return res;
                        }
                        String res = property.substring(i, next);
                        i = next + 1;
                        return res;
                    }

                };
                PropertyDescriptor currDesc;
                Object currObj = t;
                Preconditions.checkState(tokensIter.hasNext(), "Empty property for type %s", ClassUtil.classOf(t));
                boolean firstCall = true;
                do {
                    if (currObj == null)
                        break;
                    String next = tokensIter.next();
                    currDesc = BeanUtils.getPropertyDescriptor(currObj.getClass(), next);
                    if (currDesc == null && firstCall) {
                        checkPropertyExists(tokensIter.hasNext(), t, property);
                        next = tokensIter.next();
                        currDesc = BeanUtils.getPropertyDescriptor(ClassUtil.classOf(t), next);
                    }
                    firstCall = false;
                    checkPropertyExists(currDesc != null, t, property);
                    Method method = currDesc.getReadMethod();
                    Preconditions.checkState(method != null, "Can't make pivots automatically via reflection. No property accessor can be found for path %s for type %s", property, ClassUtil.classOf(t));
                    try {
                        currObj = method.invoke(currObj);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Can't access property path " + property + " for type " + ClassUtil.classOf(t));
                    } catch (InvocationTargetException e) {
                        throw new IllegalStateException("Exception in accessor method occurred for property " + property + " for type " + ClassUtil.classOf(t), e);
                    }
                } while (tokensIter.hasNext());
                if (currObj != null)
                    res.add(SeekPivot.of(property, String.valueOf(currObj)));
            }
            return res;
        }

        private void checkPropertyExists(boolean condition, T t, String property) {
            Preconditions.checkState(condition, "Can't make pivots automatically via reflection. No property can be found for path %s for type %s", property, ClassUtil.classOf(t));
        }

    }

}