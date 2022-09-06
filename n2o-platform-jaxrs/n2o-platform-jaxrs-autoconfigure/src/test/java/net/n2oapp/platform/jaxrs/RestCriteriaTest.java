package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.jaxrs.api.SomeCriteria;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static net.n2oapp.platform.jaxrs.CollectionUtil.listOf;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class RestCriteriaTest {

    @Test
    public void testMoveToFirstPage() {
        SomeCriteria someCriteria = createSomeCriteria(15);
        RestCriteria first = someCriteria.first();
        assertEquals(RestCriteria.FIRST_PAGE_NUMBER, first.getPageNumber());
        assertCriteriasEqByFiltersAndSortAndPageSize(someCriteria, (SomeCriteria) first);
    }

    @Test
    public void testPaginateForward() {
        SomeCriteria someCriteria = createSomeCriteria(54);
        RestCriteria next = someCriteria.next();
        assertEquals(someCriteria.getPageNumber() + 1, next.getPageNumber());
        assertCriteriasEqByFiltersAndSortAndPageSize(someCriteria, (SomeCriteria) next);
    }

    @Test
    public void testPaginateBackward() {
        SomeCriteria someCriteria = createSomeCriteria(3);
        RestCriteria prev = someCriteria.previous();
        assertEquals(someCriteria.getPageNumber() - 1, prev.getPageNumber());
    }

    @Test(expected = IllegalStateException.class)
    public void testPaginateBackwardWhenBeginningReached() {
        SomeCriteria someCriteria = createSomeCriteria(RestCriteria.FIRST_PAGE_NUMBER);
        someCriteria.previous();
    }

    private SomeCriteria createSomeCriteria(int pageNumber) {
        Date dateBegin = new Date();
        LocalDateTime dateEnd = LocalDateTime.now();
        String nameLike = "TEST_STR";
        int pageSize = 13;
        SomeCriteria someCriteria = new SomeCriteria(pageNumber, pageSize, Sort.by(listOf(Sort.Order.asc("nameLike"), Sort.Order.desc("dateBegin"))));
        someCriteria.setLikeName(nameLike);
        someCriteria.setDateBegin(dateBegin);
        someCriteria.setDateEnd(dateEnd);
        return someCriteria;
    }

    private void assertCriteriasEqByFiltersAndSortAndPageSize(SomeCriteria criteria1, SomeCriteria criteria2) {
        assertEquals(criteria1.getLikeName(), criteria2.getLikeName());
        assertEquals(criteria1.getDateBegin(), criteria2.getDateBegin());
        assertEquals(criteria1.getDateEnd(), criteria2.getDateEnd());
        assertEquals(criteria1.getSort(), criteria2.getSort());
        assertEquals(criteria1.getPageSize(), criteria2.getPageSize());
    }

}
