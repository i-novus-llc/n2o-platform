package net.n2oapp.platform.seek;

import net.n2oapp.platform.jaxrs.seek.RequestedPageEnum;
import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekRequest;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositePkEntityRepositoryTest extends SeekPagingTest {

    private static final String SOME_FIELD = QCompositePkEntity.compositePkEntity.someField.toString();
    private static final String FIRST = QCompositePkEntity.compositePkEntity.id.first.toString();
    private static final String SECOND = QCompositePkEntity.compositePkEntity.id.second.toString();

    private static final int N = 10;
    private static final int M = 10;

    @Autowired
    private CompositePkEntityRepository repository;

    @BeforeEach
    public void setup() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                CompositePkEntity.Id id = new CompositePkEntity.Id(i, j);
                CompositePkEntity entity = new CompositePkEntity(id, ThreadLocalRandom.current().nextInt(0, 5));
                repository.save(entity);
            }
        }
    }

    @Test
    public void test() {
        SeekRequest request = new SeekRequest();
        request.setSize(1);
        request.setPage(RequestedPageEnum.FIRST);
        request.setSort(
            Sort.by(
                List.of(
                    Sort.Order.asc(SOME_FIELD),
                    Sort.Order.asc(FIRST),
                    Sort.Order.desc(SECOND)
                )
            )
        );
        CompositePkEntity last = null;
        int count = 0;
        SeekedPage<CompositePkEntity> prev = null;
        while (true) {
            SeekedPage<CompositePkEntity> page = repository.findAll(request);
            if (prev == null) {
                prev = page;
            } else {
                assertTrue(prev.hasNext());
                assertTrue(page.hasPrev());
            }
            count += page.size();
            for (CompositePkEntity entity : page) {
                CompositePkEntity.Id id = entity.getId();
                if (last != null) {
                    if (last.getSomeField() == entity.getSomeField()) {
                        if (last.getId().getFirst() == id.getFirst()) {
                            assertTrue(last.getId().getSecond() > id.getSecond());
                        } else {
                            assertTrue(last.getId().getFirst() < id.getFirst());
                        }
                    } else
                        assertTrue(last.getSomeField() < entity.getSomeField());
                }
                last = entity;
            }
            if (!page.hasNext())
                break;
            request.setPivots(List.of(
                    SeekPivot.of(SOME_FIELD, String.valueOf(last.getSomeField())),
                    SeekPivot.of(FIRST, String.valueOf(last.getId().getFirst())),
                    SeekPivot.of(SECOND, String.valueOf(last.getId().getSecond()))
            ));
            request.setPage(RequestedPageEnum.NEXT);
        }
        assertEquals(N * M, count);
        request.setPage(RequestedPageEnum.LAST);
        SeekedPageIterator<CompositePkEntity, SeekRequest> iter = SeekedPageIterator.from(
            repository::findAll,
            request
        );
        count = 0;
        while (iter.hasNext()) {
            SeekedPage<CompositePkEntity> next = iter.next();
            count += next.size();
        }
        assertEquals(N * M, count);
    }

}
