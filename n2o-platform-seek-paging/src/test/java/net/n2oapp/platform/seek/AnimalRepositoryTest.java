package net.n2oapp.platform.seek;

import net.n2oapp.platform.jaxrs.seek.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.*;
import static org.junit.jupiter.api.Assertions.*;

public class AnimalRepositoryTest extends SeekPagingTest {

    private static final Comparator<Animal> CMP =
        comparing(Animal::getBirthDate, nullsLast(reverseOrder())).
            thenComparing(animal -> mapNullable(animal.getFavoriteFood(), Food::getName), nullsFirst(naturalOrder())).
            thenComparing(animal -> mapNullable(animal.getParent(), Animal::getName), nullsFirst(reverseOrder())).
            thenComparing(Animal::getHeight, nullsLast(naturalOrder())).
            thenComparing(Animal::getId, nullsFirst(naturalOrder()));

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

    private final Set<Animal> animals = new TreeSet<>(CMP);
    private final List<Food> foods = new ArrayList<>();

    @Before
    @Transactional
    public void setup() {
        for (int i = 0; i < 5; i++) {
            Food food = foodRepository.save(new Food(null, randomString(3)));
            foods.add(food);
        }
        animals.clear();
        repository.deleteAll();
        List<Animal> animalsList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            Animal animal = new Animal(
                null,
                randomString(1),
                randomLocalDate(),
                randomFrom(animalsList),
                randomBigInteger(),
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
        LocalDate prevBirthDate = null;
        String prevFavoriteFood = null;
        String prevParentName = null;
        BigInteger prevHeight = null;
        Integer prevId = null;
        SeekedPage<Animal> prevPage = null;
        List<SeekedPage<Animal>> pageSequence = new ArrayList<>();
        SeekableCriteria criteria = new EmptySeekableCriteria();
        criteria.setPage(RequestedPageEnum.FIRST);
        criteria.setSize(1);
        criteria.setOrders(List.of(
            Sort.Order.desc(BIRTH_DATE).nullsLast(),
            Sort.Order.asc(FAV_FOOD).nullsFirst(),
            Sort.Order.desc(PARENT).nullsFirst(),
            Sort.Order.asc(HEIGHT).nullsLast(),
            Sort.Order.asc(ID).nullsFirst()
        ));
        Iterator<Animal> animalsIterator = animals.iterator();
        while (true) {
            SeekedPage<Animal> page = repository.findAll(criteria);
            criteria.setPage(RequestedPageEnum.NEXT);
            if (!page.isEmpty()) {
                if (prevPage != null) {
                    assertTrue(prevPage.hasNext());
//                    assertTrue(page.hasPrev());
                }
                for (Animal animal : page) {
                    Animal next = animalsIterator.next();
                    assertEquals(animal, next);
                    prevBirthDate = animal.getBirthDate();
                    prevFavoriteFood = mapNullable(animal.getFavoriteFood(), Food::getName);
                    prevParentName = mapNullable(animal.getParent(), Animal::getName);
                    prevHeight = animal.getHeight();
                    prevId = animal.getId();
                    animalsIterator.remove();
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
//            assertTrue(page.hasNext());
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
                    prevFavoriteFood = mapNullable(actual.getFavoriteFood(), Food::getName);
                    prevParentName = mapNullable(actual.getParent(), Animal::getName);
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
        SeekedPageIterator<Animal, SeekableCriteria> pageIterator = SeekedPageIterator.from(
            c -> repository.findAll(c),
            criteria
        );
        int count = 0;
        pageSequence.clear();
        animalsIterator = animals.iterator();
        while (pageIterator.hasNext()) {
            SeekedPage<Animal> page = pageIterator.next();
            count += page.size();
            assertTrue(page.size() > 0);
            pageSequence.add(page);
            for (Animal next : page) {
                assertEquals(next, animalsIterator.next());
            }
        }
        assertEquals(N, count);
        try {
            pageIterator.next();
            fail("Exception expected");
        } catch (NoSuchElementException ignored) {}
        criteria.setPage(RequestedPageEnum.LAST);
        pageIterator = SeekedPageIterator.from(
            c -> repository.findAll(c),
            criteria
        );
        count = 0;
        while (pageIterator.hasNext()) {
            SeekedPage<Animal> next = pageIterator.next();
            assertEquals(pageSequence.remove(pageSequence.size() - 1).getContent(), next.getContent());
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

    private static  <E, M> M mapNullable(E element, Function<? super E, ? extends M> mapper) {
        if (element == null)
            return null;
        return mapper.apply(element);
    }

    private void setPivots(LocalDate prevBirthDate, String prevFavoriteFood, String prevParentName, BigInteger prevHeight, Integer prevId, SeekableCriteria criteria) {
        criteria.setPivots(getPivots(prevBirthDate, prevFavoriteFood, prevParentName, prevHeight, prevId));
    }

    private List<SeekPivot> getPivots(LocalDate prevBirthDate, String prevFavoriteFood, String prevParentName, BigInteger prevHeight, Integer prevId) {
        List<SeekPivot> result = new ArrayList<>();
        if (prevBirthDate != null) result.add(SeekPivot.of(BIRTH_DATE, prevBirthDate.toString()));
        if (prevFavoriteFood != null) result.add(SeekPivot.of(FAV_FOOD, prevFavoriteFood));
        if (prevParentName != null) result.add(SeekPivot.of(PARENT, prevParentName));
        if (prevHeight != null) result.add(SeekPivot.of(HEIGHT, prevHeight.toString()));
        if (prevId != null) result.add(SeekPivot.of(ID, prevId.toString()));
        return result;
    }

}