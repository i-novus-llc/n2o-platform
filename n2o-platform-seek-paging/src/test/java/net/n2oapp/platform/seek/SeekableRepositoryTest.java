package net.n2oapp.platform.seek;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeekableRepositoryTest extends SeekPagingTest {

    @Autowired
    AnimalRepository repository;

    private final List<AnimalEntity> animals = new ArrayList<>();
    private final int n = 500;

    @Before
    public void setup() {
        for (int i = 0; i < n; i++) {
            AnimalEntity entity = new AnimalEntity(null, randomString(10), randomLocalDate());
            entity = repository.save(entity);
            animals.add(entity);
        }
        animals.sort(Comparator.comparingInt(AnimalEntity::getId));
    }

    @Test
    public void testSeek() {
        SeekableCriteria criteria = new SeekableCriteria();
        criteria.setOrders(List.of(Sort.Order.asc("id")));
        criteria.setPivots(List.of(new SeekPivot("id", "0")));
        Iterator<AnimalEntity> iter = animals.iterator();
        String lastSeenId = "0";
        for (int i = 0; i < n; i++) {
            List<AnimalEntity> all = repository.findAll(criteria);
            if (!all.isEmpty()) {
                for (AnimalEntity actual : all) {
                    AnimalEntity expected = iter.next();
                    assertEquals(expected.getId(), actual.getId());
                    iter.remove();
                    lastSeenId = String.valueOf(expected.getId());
                }
            } else
                break;
            criteria.setPivots(singletonList(new SeekPivot("id", lastSeenId)));
        }
        assertTrue(animals.isEmpty());
    }

}
