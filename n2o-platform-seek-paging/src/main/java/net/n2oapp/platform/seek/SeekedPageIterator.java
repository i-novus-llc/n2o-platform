package net.n2oapp.platform.seek;

import com.google.common.base.Preconditions;
import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekableCriteria;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static net.n2oapp.platform.jaxrs.seek.RequestedPageEnum.*;

public class SeekedPageIterator<T, C extends SeekableCriteria> implements Iterator<SeekedPage<T>> {

    private final Function<? super C, SeekedPage<T>> pageSource;
    private final Function<? super T, List<SeekPivot>> pivotsMaker;
    private final C criteria;

    private SeekedPage<T> next;
    private boolean hasNextCalled;
    private final boolean forward;

    private SeekedPageIterator(
        Function<? super C, SeekedPage<T>> pageSource,
        Function<? super T, List<SeekPivot>> pivotsMaker,
        C criteria
    ) {
        this.pageSource = pageSource;
        this.pivotsMaker = pivotsMaker;
        this.criteria = criteria;
        this.forward = criteria.getPage() == NEXT || criteria.getPage() == FIRST;
    }

    /**
     * Устанавливает курсор на первую страницу
     */
    public void seekToFirst() {
        Preconditions.checkState(forward, "This iterator goes only forwards");
        criteria.setPage(FIRST);
        hasNextCalled = false;
    }

    /**
     * Устанавливает курсор на последнюю страницу
     */
    public void seekToLast() {
        Preconditions.checkState(!forward, "This iterator goes only backwards");
        criteria.setPage(LAST);
        hasNextCalled = false;
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
                criteria.setPivots(pivotsMaker.apply(content.get(content.size() - 1)));
                if (criteria.getPage() == FIRST)
                    criteria.setPage(NEXT);
            } else {
                criteria.setPivots(pivotsMaker.apply(content.get(0)));
                if (criteria.getPage() == LAST)
                    criteria.setPage(PREV);
            }
        } else {
            next = null;
        }
    }

    public static <T, C extends SeekableCriteria> SeekedPageIterator<T, C> from(
        Function<? super C, SeekedPage<T>> pageSource,
        Function<? super T, List<SeekPivot>> pivotsMaker,
        C criteria
    ) {
        return new SeekedPageIterator<>(pageSource, pivotsMaker, criteria);
    }

}
