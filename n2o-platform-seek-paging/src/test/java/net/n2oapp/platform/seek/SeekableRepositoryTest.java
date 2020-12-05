package net.n2oapp.platform.seek;

import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekableCriteria;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.math.BigInteger;
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
    private final int n = 2500;
    private String maxAnimalName;

    @Before
    public void setup() {
        for (int i = 0; i < 15; i++) {
            Food food = foodRepository.save(new Food(null, randomString(3)));
            foods.add(food);
        }
        animals.clear();
        repository.deleteAll();
        List<Animal> animalsList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Animal animal = new Animal(null,
                randomString(3),
                randomLocalDate(),
                randomFrom(animalsList),
                BigInteger.valueOf(ThreadLocalRandom.current().nextInt(1, 10)),
                randomFrom(foods)
            );
            if (animal.getParent() == null)
                animal.setParent(animal);
            animal = repository.save(animal);
            animals.add(animal);
            animalsList.add(animal);
            maxAnimalName = maxAnimalName == null ? animal.getName() : animal.getName().compareTo(maxAnimalName) > 0 ? animal.getName() : maxAnimalName;
        }
    }

    @Test
    public void testSeek() {
        LocalDate prevBirthDate = LocalDate.of(2077, 1, 1);
        String prevFavoriteFood = "";
        String prevParentName = maxAnimalName;
        BigInteger prevHeight = BigInteger.ZERO;
        Integer prevId = 0;
        SeekedPage<Animal> prevPage = null;
        List<SeekedPage<Animal>> pageSequence = new ArrayList<>();
        SeekableCriteria criteria = new SeekableCriteria();
        criteria.setSize(10);
        criteria.setOrders(List.of(
            Sort.Order.desc("birthDate"),
            Sort.Order.asc("favoriteFood.name"),
            Sort.Order.desc("parent.name"),
            Sort.Order.asc("height"),
            Sort.Order.asc("id")
        ));
        setPivots(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId, criteria);
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
                        int cmp1 = prevBirthDate.compareTo(animal.getBirthDate());
                        if (cmp1 == 0) {
                            int cmp2 = prevFavoriteFood.compareTo(animal.getFavoriteFood().getName());
                            if (cmp2 == 0) {
                                int cmp3 = prevParentName.compareTo(animal.getParent().getName());
                                if (cmp3 == 0) {
                                    int cmp4 = prevHeight.compareTo(animal.getHeight());
                                    if (cmp4 == 0)
                                        assertTrue(prevId.compareTo(animal.getId()) < 0);
                                    else
                                        assertTrue(cmp4 < 0);
                                } else
                                    assertTrue(cmp3 > 0);
                            } else
                                assertTrue(cmp2 < 0);
                        } else {
                            assertTrue(cmp1 > 0);
                        }
                    }
                    prevBirthDate = animal.getBirthDate();
                    prevFavoriteFood = animal.getFavoriteFood().getName();
                    prevParentName = animal.getParent().getName();
                    prevHeight = animal.getHeight();
                    prevId = animal.getId();
                }
            } else {
                assertFalse(page.hasNext());
                assertTrue(page.hasPrev());
                break;
            }
            pageSequence.add(page);
            prevPage = page;
            setPivots(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId, criteria);
        }
        assertEquals(0, animals.size());
        prevPage = pageSequence.remove(pageSequence.size() - 1);
        animals.addAll(prevPage.getContent());
        Animal animal = prevPage.getContent().get(0);
        prevBirthDate = animal.getBirthDate();
        prevFavoriteFood = animal.getFavoriteFood().getName();
        prevParentName = animal.getParent().getName();
        prevHeight = animal.getHeight();
        prevId = animal.getId();
        setPivots(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId, criteria);
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
                    prevBirthDate = actual.getBirthDate();
                    prevFavoriteFood = actual.getFavoriteFood().getName();
                    prevParentName = actual.getParent().getName();
                    prevHeight = actual.getHeight();
                    prevId = actual.getId();
                }
            } else {
                assertFalse(page.hasPrev());
                break;
            }
            setPivots(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId, criteria);
        }
        assertEquals(n, animals.size());
    }

    private void setPivots(LocalDate prevBirthDate, String prevFavoriteFood, String prevParentName, BigInteger prevHeight, Integer prevId, SeekableCriteria criteria) {
        criteria.setPivots(List.of(
            SeekPivot.of("birthDate", prevBirthDate.toString()),
            SeekPivot.of("favoriteFood.name", prevFavoriteFood),
            SeekPivot.of("parent.name", prevParentName),
            SeekPivot.of("height", prevHeight.toString()),
            SeekPivot.of("id", prevId.toString())
        ));
    }

}
