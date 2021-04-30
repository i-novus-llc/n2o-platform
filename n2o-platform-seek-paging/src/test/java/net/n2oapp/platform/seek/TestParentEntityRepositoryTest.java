package net.n2oapp.platform.seek;

import net.n2oapp.platform.jaxrs.seek.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestParentEntityRepositoryTest extends SeekPagingTest {

    private static final Comparator<TestParentEntity> CMP =
            comparing(TestParentEntity::getField2, nullsLast(reverseOrder())).
            thenComparing(entity -> mapNullable(entity.getChild(), TestChildEntity::getField1), nullsFirst(naturalOrder())).
            thenComparing(entity -> mapNullable(entity.getParent(), TestParentEntity::getField1), nullsFirst(reverseOrder())).
            thenComparing(TestParentEntity::getField3, nullsLast(naturalOrder())).
            thenComparing(TestParentEntity::getId, nullsFirst(naturalOrder()));

    private static final int N = 1000;

    private static final String FIELD_2 = stripUntilFirstDot(QTestParentEntity.testParentEntity.field2.toString());
    private static final String CHILD_FIELD_1 = stripUntilFirstDot(QTestParentEntity.testParentEntity.child.field1.toString());
    private static final String PARENT_FIELD_1 = stripUntilFirstDot(QTestParentEntity.testParentEntity.parent.field1.toString());
    private static final String FIELD_3 = stripUntilFirstDot(QTestParentEntity.testParentEntity.field3.toString());
    private static final String ID = stripUntilFirstDot(QTestParentEntity.testParentEntity.id.toString());

    private static final List<Sort.Order> ORDERS = List.of(
            Sort.Order.desc(FIELD_2).nullsLast(),
            Sort.Order.asc(CHILD_FIELD_1).nullsFirst(),
            Sort.Order.desc(PARENT_FIELD_1).nullsFirst(),
            Sort.Order.asc(FIELD_3).nullsLast(),
            Sort.Order.asc(ID).nullsFirst()
    );

    @Autowired
    TestParentEntityRepository repository;

    @Autowired
    TestChildEntityRepository testChildEntityRepository;

    private final Set<TestParentEntity> parentEntities = new TreeSet<>(CMP);
    private final List<TestChildEntity> childEntities = new ArrayList<>();

    @Before
    public void setup() {
        for (int i = 0; i < 50; i++) {
            TestChildEntity food = testChildEntityRepository.save(new TestChildEntity(null, randomInteger()));
            childEntities.add(food);
        }
        parentEntities.clear();
        repository.deleteAll();
        List<TestParentEntity> animalsList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            TestParentEntity entity = new TestParentEntity(
                null,
                randomInteger(),
                randomInteger(),
                randomFrom(animalsList),
                randomInteger(),
                randomFrom(childEntities)
            );
            if (entity.getParent() == null)
                entity.setParent(entity);
            entity = repository.save(entity);
            this.parentEntities.add(entity);
            animalsList.add(entity);
        }
    }

    @Test
    public void testSeek() {
        Integer prevField2 = null;
        Integer prevChildField1 = null;
        Integer prevParentField1 = null;
        Integer prevField3 = null;
        Integer prevId = null;
        SeekedPage<TestParentEntity> prevPage = null;
        List<SeekedPage<TestParentEntity>> pageSequence = new ArrayList<>();
        SeekRequest request = new SeekRequest();
        request.setPage(RequestedPageEnum.FIRST);
        request.setSize(1);
        request.setSort(Sort.by(ORDERS));
        Iterator<TestParentEntity> parentEntitiesIterator = this.parentEntities.iterator();
        while (true) {
            SeekedPage<TestParentEntity> page = repository.findAll(request);
            request.setPage(RequestedPageEnum.NEXT);
            if (!page.isEmpty()) {
                if (prevPage != null) {
                    assertTrue(prevPage.hasNext());
                    assertTrue(page.hasPrev());
                }
                for (TestParentEntity entity : page) {
                    TestParentEntity next = parentEntitiesIterator.next();
                    assertEquals(entity, next);
                    prevField2 = entity.getField2();
                    prevChildField1 = mapNullable(entity.getChild(), TestChildEntity::getField1);
                    prevParentField1 = mapNullable(entity.getParent(), TestParentEntity::getField1);
                    prevField3 = entity.getField3();
                    prevId = entity.getId();
                    parentEntitiesIterator.remove();
                }
            } else {
                assertFalse(page.hasNext());
                assertTrue(page.hasPrev());
                break;
            }
            pageSequence.add(page);
            prevPage = page;
            setPivots(prevField2, prevChildField1, prevParentField1, prevField3, prevId, request);
        }
        assertEquals(0, this.parentEntities.size());
        prevPage = pageSequence.remove(pageSequence.size() - 1);
        this.parentEntities.addAll(prevPage.getContent());
        TestParentEntity entity = prevPage.getContent().get(0);
        prevField2 = entity.getField2();
        prevChildField1 = mapNullable(entity, TestParentEntity::getChild).getField1();
        prevParentField1 = mapNullable(entity.getParent(), TestParentEntity::getField1);
        prevField3 = entity.getField3();
        prevId = entity.getId();
        setPivots(prevField2, prevChildField1, prevParentField1, prevField3, prevId, request);
        request.setPage(RequestedPageEnum.PREV);
        while (true) {
            SeekedPage<TestParentEntity> page = repository.findAll(request);
            if (!page.hasNext()) {
                repository.findAll(request);
            }
            assertTrue(page.hasNext());
            if (!page.isEmpty()) {
                assertFalse(pageSequence.isEmpty());
                SeekedPage<TestParentEntity> expectedPage = pageSequence.remove(pageSequence.size() - 1);
                assertEquals(expectedPage.size(), page.size());
                if (!pageSequence.isEmpty())
                    assertTrue(page.hasPrev());
                ListIterator<TestParentEntity> iter1 = ((SeekedPageImpl<TestParentEntity>) page).listIteratorFromTheEnd();
                ListIterator<TestParentEntity> iter2 = ((SeekedPageImpl<TestParentEntity>) expectedPage).listIteratorFromTheEnd();
                while (iter1.hasPrevious()) {
                    TestParentEntity actual = iter1.previous();
                    TestParentEntity expected = iter2.previous();
                    assertTrue(this.parentEntities.add(actual));
                    assertEquals(expected, actual);
                    prevField2 = actual.getField2();
                    prevChildField1 = mapNullable(actual.getChild(), TestChildEntity::getField1);
                    prevParentField1 = mapNullable(actual.getParent(), TestParentEntity::getField1);
                    prevField3 = actual.getField3();
                    prevId = actual.getId();
                }
            } else {
                assertFalse(page.hasPrev());
                break;
            }
            setPivots(prevField2, prevChildField1, prevParentField1, prevField3, prevId, request);
        }
        assertEquals(N, this.parentEntities.size());
        request.setPage(RequestedPageEnum.FIRST);
        SeekedPageIterator<TestParentEntity, SeekRequest> pageIterator = SeekedPageIterator.from(
            c -> repository.findAll(c),
            request
        );
        int count = 0;
        pageSequence.clear();
        parentEntitiesIterator = this.parentEntities.iterator();
        while (pageIterator.hasNext()) {
            SeekedPage<TestParentEntity> page = pageIterator.next();
            count += page.size();
            assertTrue(page.size() > 0);
            pageSequence.add(page);
            for (TestParentEntity next : page) {
                assertEquals(next, parentEntitiesIterator.next());
            }
        }
        assertEquals(N, count);
        try {
            pageIterator.next();
            fail("Exception expected");
        } catch (NoSuchElementException ignored) {}
        request.setPage(RequestedPageEnum.LAST);
        pageIterator = SeekedPageIterator.from(
            c -> repository.findAll(c),
            request
        );
        count = 0;
        while (pageIterator.hasNext()) {
            SeekedPage<TestParentEntity> next = pageIterator.next();
            assertEquals(pageSequence.remove(pageSequence.size() - 1).getContent(), next.getContent());
            count += next.size();
        }
        assertEquals(N, count);
    }

    @Test
    public void testBoundaries() {
        SeekRequest request = new SeekRequest();
        request.setSort(Sort.by(List.of(Sort.Order.asc(ID))));
        request.setPage(RequestedPageEnum.FIRST);
        request.setSize(7);
        Function<Seekable, SeekedPage<TestParentEntity>> pageSource = c -> repository.findAll(c);
        SeekedPageIterator<TestParentEntity, SeekRequest> iter = SeekedPageIterator.from(pageSource, request);
        long total = 0;
        while (iter.hasNext()) {
            SeekedPage<TestParentEntity> page = iter.next();
            total += page.size();
        }
        assertEquals(N, total);
        request.setPage(RequestedPageEnum.LAST);
        iter = SeekedPageIterator.from(pageSource, request);
        total = 0;
        while (iter.hasNext()) {
            SeekedPage<TestParentEntity> page = iter.next();
            total += page.size();
        }
        assertEquals(N, total);
    }

    private static <E, M> M mapNullable(E element, Function<? super E, ? extends M> mapper) {
        if (element == null)
            return null;
        return mapper.apply(element);
    }

    private void setPivots(
            Integer prevField2,
            Integer prevChildField1,
            Integer prevParentField1,
            Integer prevField3,
            Integer prevId,
            SeekRequest request
    ) {
        request.setPivots(getPivots(prevField2, prevChildField1, prevParentField1, prevField3, prevId));
    }

    private List<SeekPivot> getPivots(
            Integer prevField2,
            Integer prevChildField1,
            Integer prevParentField1,
            Integer prevField3,
            Integer prevId
    ) {
        List<SeekPivot> result = new ArrayList<>();
        if (prevField2 != null) result.add(SeekPivot.of(FIELD_2, prevField2.toString()));
        if (prevChildField1 != null) result.add(SeekPivot.of(CHILD_FIELD_1, String.valueOf(prevChildField1)));
        if (prevParentField1 != null) result.add(SeekPivot.of(PARENT_FIELD_1, String.valueOf(prevParentField1)));
        if (prevField3 != null) result.add(SeekPivot.of(FIELD_3, prevField3.toString()));
        if (prevId != null) result.add(SeekPivot.of(ID, prevId.toString()));
        return result;
    }

    private static String stripUntilFirstDot(String str) {
        int dot = str.indexOf('.');
        return str.substring(dot + 1);
    }

}