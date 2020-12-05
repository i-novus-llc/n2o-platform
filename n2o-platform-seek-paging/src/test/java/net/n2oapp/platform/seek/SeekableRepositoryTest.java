package net.n2oapp.platform.seek;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

public class SeekableRepositoryTest extends SeekPagingTest {

    @Autowired
    AnimalRepository repository;

    @Autowired
    FoodRepository foodRepository;

    private final Set<Animal> animals = new HashSet<>();
    private final List<Food> foods = new ArrayList<>();
    private final int n = 500;

    @Before
    public void setup() {
        for (int i = 0; i < n / 100; i++) {
            Food food = foodRepository.save(new Food(null, randomString(10)));
            foods.add(food);
        }
        animals.clear();
        repository.deleteAll();
        List<Animal> animalsList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Animal entity = new Animal(null,
                randomString(10),
                randomLocalDate(),
                randomFrom(animalsList),
                BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 1000)),
                BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 150)),
                randomFrom(foods)
            );
            entity = repository.save(entity);
            animals.add(entity);
            animalsList.add(entity);
        }
    }

    @Test
    public void testSeek() {
        SeekedPage<Animal> prevPage = null;
        List<SeekedPage<Animal>> pageSequence = new ArrayList<>();
        SeekableCriteria criteria = new SeekableCriteria();
        criteria.setSize(10);
        criteria.setOrders(List.of(Sort.Order.desc("birthDate"), Sort.Order.asc("id")));
        criteria.setPivots(List.of(new SeekPivot("id", "0"), new SeekPivot("birthDate", "2077-01-01")));
        Integer prevId = null;
        LocalDate prevBirthDate = null;
        while (true) {
            SeekedPage<Animal> page = repository.findAll(criteria);
            if (!page.isEmpty()) {
                if (prevPage != null) {
                    assertTrue(prevPage.hasNext());
                    assertTrue(page.hasPrev());
                }
                for (Animal animal : page) {
                    assertTrue(animals.remove(animal));
                    if (prevId != null) {
                        if (animal.getBirthDate().compareTo(prevBirthDate) == 0) {
                            assertTrue(prevId.compareTo(animal.getId()) < 0);
                        } else {
                            assertTrue(prevBirthDate.compareTo(animal.getBirthDate()) > 0);
                        }
                    }
                    prevId = animal.getId();
                    prevBirthDate = animal.getBirthDate();
                }
            } else {
                assertFalse(page.hasNext());
                assertTrue(page.hasPrev());
                break;
            }
            pageSequence.add(page);
            prevPage = page;
            criteria.setPivots(List.of(new SeekPivot("id", prevId.toString()), new SeekPivot("birthDate", prevBirthDate.toString())));
        }
        assertTrue(animals.isEmpty());
        prevPage = pageSequence.remove(pageSequence.size() - 1);
        animals.addAll(prevPage.getContent());
        prevId = prevPage.getContent().get(0).getId();
        prevBirthDate = prevPage.getContent().get(0).getBirthDate();
        criteria.setPivots(List.of(new SeekPivot("id", prevId.toString()), new SeekPivot("birthDate", prevBirthDate.toString())));
        criteria.setPrev(true);
        while (true) {
            SeekedPage<Animal> page = repository.findAll(criteria);
            assertTrue(page.hasNext());
            if (!page.isEmpty()) {
                assertFalse(pageSequence.isEmpty());
                SeekedPage<Animal> expectedPage = pageSequence.remove(pageSequence.size() - 1);
                assertEquals(expectedPage.size(), page.size());
                if (!pageSequence.isEmpty())
                    assertTrue(page.hasPrev());
                ListIterator<Animal> iter1 = page.listIteratorFromTheEnd();
                ListIterator<Animal> iter2 = expectedPage.listIteratorFromTheEnd();
                while (iter1.hasPrevious()) {
                    Animal actual = iter1.previous();
                    Animal expected = iter2.previous();
                    assertTrue(animals.add(actual));
                    assertEquals(expected, actual);
                    prevId = actual.getId();
                    prevBirthDate = actual.getBirthDate();
                }
            } else {
                assertFalse(page.hasPrev());
                break;
            }
            criteria.setPivots(List.of(new SeekPivot("id", prevId.toString()), new SeekPivot("birthDate", prevBirthDate.toString())));
        }
        assertEquals(n, animals.size());
    }

}
