package net.n2oapp.platform.seek;

import net.n2oapp.platform.jaxrs.seek.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class AnimalRepositoryTest extends SeekPagingTest {

    private static final int N = 2500;

    private static final String BIRTH_DATE = QAnimal.animal.birthDate.toString();
    private static final String FAV_FOOD = QAnimal.animal.favoriteFood.name.toString();
    private static final String PARENT = QAnimal.animal.parent.name.toString();
    private static final String HEIGHT = QAnimal.animal.height.toString();
    private static final String ID = QAnimal.animal.id.toString();

    @Autowired
    AnimalRepository repository;

    @Autowired
    FoodRepository foodRepository;

    private final Set<Animal> animals = new HashSet<>();
    private final List<Food> foods = new ArrayList<>();

    @Before
    public void setup() {
        for (int i = 0; i < 5; i++) {
            Food food = foodRepository.save(new Food(null, randomString(3)));
            foods.add(food);
        }
        animals.clear();
        repository.deleteAll();
        List<Animal> animalsList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            Animal animal = new Animal(null,
                randomString(1),
                randomLocalDate(),
                randomFrom(animalsList),
                BigInteger.valueOf(ThreadLocalRandom.current().nextInt(1, 4)),
                randomFrom(foods)
            );
            if (animal.getParent() == null)
                animal.setParent(animal);
            animal = repository.save(animal);
            animals.add(animal);
            animalsList.add(animal);
        }
    }

    @Test
    public void testSeek() {
        LocalDate prevBirthDate = LocalDate.of(2077, 1, 1);
        String prevFavoriteFood = "";
        String prevParentName = Character.toString(0x10FFFF);
        BigInteger prevHeight = BigInteger.ZERO;
        Integer prevId = 1;
        SeekedPage<Animal> prevPage = null;
        List<SeekedPage<Animal>> pageSequence = new ArrayList<>();
        SeekableCriteria criteria = new EmptySeekableCriteria();
        criteria.setPage(RequestedPageEnum.NEXT);
        criteria.setSize(100);
        criteria.setOrders(List.of(
            Sort.Order.desc(BIRTH_DATE),
            Sort.Order.asc(FAV_FOOD),
            Sort.Order.desc(PARENT),
            Sort.Order.asc(HEIGHT),
            Sort.Order.asc(ID)
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
                    checkSorted(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId, animal);
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
        criteria.setPage(RequestedPageEnum.PREV);
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
        assertEquals(N, animals.size());
        criteria.setPage(RequestedPageEnum.FIRST);
        SeekedPageIterator<Animal, SeekableCriteria> iter = SeekedPageIterator.from(
            c -> repository.findAll(c),
            criteria
        );
        int count = 0;
        Animal prev = null;
        pageSequence.clear();
        while (iter.hasNext()) {
            SeekedPage<Animal> page = iter.next();
            count += page.size();
            assertTrue(page.size() > 0);
            pageSequence.add(page);
            for (Animal next : page) {
                if (prev == null)
                    prev = next;
                else {
                    checkSorted(prev.getBirthDate(), prev.getFavoriteFood().getName(), prev.getParent().getName(), prev.getHeight(), prev.getId(), next);
                }
            }
        }
        assertEquals(N, count);
        try {
            iter.next();
            fail("Exception expected");
        } catch (NoSuchElementException ignored) {}
        criteria.setPage(RequestedPageEnum.LAST);
        iter = SeekedPageIterator.from(
            c -> repository.findAll(c),
            criteria
        );
        count = 0;
        while (iter.hasNext()) {
            SeekedPage<Animal> next = iter.next();
            assertEquals(pageSequence.remove(pageSequence.size() - 1), next);
            count += next.size();
        }
        assertEquals(N, count);
    }

    @Test
    public void testBoundaries() {
        SeekableCriteria criteria = new EmptySeekableCriteria();
        criteria.setOrders(List.of(Sort.Order.asc(ID)));
        criteria.setPage(RequestedPageEnum.FIRST);
        criteria.setSize(7);
        Function<Animal, List<SeekPivot>> pivotsMaker = animal -> List.of(SeekPivot.of(ID, animal.getId().toString()));
        Function<SeekableCriteria, SeekedPage<Animal>> pageSource = c -> repository.findAll(c);
        SeekedPageIterator<Animal, SeekableCriteria> iter = SeekedPageIterator.from(pageSource, criteria);
        long total = 0;
        while (iter.hasNext()) {
            SeekedPage<Animal> page = iter.next();
            total += page.size();
        }
        assertEquals(N, total);
        criteria.setPage(RequestedPageEnum.LAST);
        iter = SeekedPageIterator.from(pageSource, criteria);
        total = 0;
        while (iter.hasNext()) {
            SeekedPage<Animal> page = iter.next();
            total += page.size();
        }
        assertEquals(N, total);
    }

    private void checkSorted(LocalDate prevBirthDate, String prevFavoriteFood, String prevParentName, BigInteger prevHeight, Integer prevId, Animal animal) {
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
    }

    private void setPivots(LocalDate prevBirthDate, String prevFavoriteFood, String prevParentName, BigInteger prevHeight, Integer prevId, SeekableCriteria criteria) {
        criteria.setPivots(getPivots(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId));
    }

    private List<SeekPivot> getPivots(LocalDate prevBirthDate, String prevFavoriteFood, String prevParentName, BigInteger prevHeight, Integer prevId) {
        return List.of(
            SeekPivot.of(BIRTH_DATE, prevBirthDate.toString()),
            SeekPivot.of(FAV_FOOD, prevFavoriteFood),
            SeekPivot.of(PARENT, prevParentName),
            SeekPivot.of(HEIGHT, prevHeight.toString()),
            SeekPivot.of(ID, prevId.toString())
        );
    }

}
