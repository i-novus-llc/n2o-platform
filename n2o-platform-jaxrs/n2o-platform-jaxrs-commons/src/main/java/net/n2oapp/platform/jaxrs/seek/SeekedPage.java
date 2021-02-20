package net.n2oapp.platform.jaxrs.seek;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Страница данных, полученная путем seek-пагинации
 * @param <T> Тип элементов
 */
public class SeekedPage<T> implements Streamable<T> {

    private static final SeekedPage<?> EMPTY = new SeekedPage<>(Collections.emptyList(), false, false);

    private final List<T> content;
    private final boolean hasNext;
    private final boolean hasPrev;

    private SeekedPage(
        @JsonProperty("content") List<T> content,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("hasPrev") boolean hasPrev
    ) {
        if (content == null)
            throw new IllegalArgumentException("Content must not be null");
        this.content = content;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }

    /**
     * @return Список данных, отсортированный в соответствии с {@link SeekableCriteria#getOrders()}.
     * Любые изменения на списке отображаются и на странице.
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Есть ли страницы после этой?
     */
    @JsonProperty("hasNext")
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Есть ли страницы перед этой?
     */
    @JsonProperty("hasPrev")
    public boolean hasPrev() {
        return hasPrev;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @JsonIgnore
    public int size() {
        return content.size();
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return content.iterator();
    }

    public ListIterator<T> listIterator() {
        return content.listIterator();
    }

    public ListIterator<T> listIteratorFromTheEnd() {
        return listIterator(content.size());
    }

    public ListIterator<T> listIterator(int idx) {
        return content.listIterator(idx);
    }

    @NonNull
    public <E> SeekedPage<E> map(@NonNull Function<? super T, ? extends E> mapper) {
        return SeekedPage.of(content.stream().map(mapper).collect(Collectors.toList()), hasNext, hasPrev);
    }

    @NonNull
    @Override
    public List<T> toList() {
        return content;
    }

    @NonNull
    @Override
    public Set<T> toSet() {
        return new HashSet<>(content);
    }

    @NonNull
    @Override
    public Stream<T> stream() {
        return get();
    }

    @NonNull
    @Override
    public Stream<T> get() {
        return content.stream();
    }

    @NonNull
    @Override
    public SeekedPage<T> filter(@NonNull Predicate<? super T> predicate) {
        return SeekedPage.of(content.stream().filter(predicate).collect(Collectors.toList()), hasNext, hasPrev);
    }

    @NonNull
    @Override
    public <R> SeekedPage<R> flatMap(@NonNull Function<? super T, ? extends Stream<? extends R>> mapper) {
        return SeekedPage.of(content.stream().flatMap(mapper).collect(Collectors.toList()), hasNext, hasPrev);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeekedPage)) return false;
        SeekedPage<?> that = (SeekedPage<?>) o;
        return hasNext == that.hasNext && hasPrev == that.hasPrev && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, hasNext, hasPrev);
    }

    @Override
    public String toString() {
        return "SeekedPage{" +
            "content=" + content +
            ", hasNext=" + hasNext +
            ", hasPrev=" + hasPrev +
            '}';
    }

    /**
     * @return Пустая страница
     */
    @SuppressWarnings("unchecked")
    public static <T> SeekedPage<T> empty() {
        return (SeekedPage<T>) EMPTY;
    }

    public static <T> SeekedPage<T> of(List<T> content, boolean hasNext, boolean hasPrev) {
        return new SeekedPage<>(content, hasNext, hasPrev);
    }

}
