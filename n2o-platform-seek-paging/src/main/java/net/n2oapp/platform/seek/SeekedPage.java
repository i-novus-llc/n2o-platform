package net.n2oapp.platform.seek;

import com.google.common.base.Preconditions;
import org.springframework.lang.NonNull;

import java.util.*;

public class SeekedPage<T> implements Iterable<T> {

    private static final SeekedPage<?> EMPTY = new SeekedPage<>(Collections.emptyList(), false, false);

    private final List<T> content;
    private final boolean hasNext;
    private final boolean hasPrev;

    private SeekedPage(List<T> content, boolean hasNext, boolean hasPrev) {
        this.content = Preconditions.checkNotNull(content);
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }

    public List<T> getContent() {
        return content;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public boolean hasPrev() {
        return hasPrev;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeekedPage)) return false;
        SeekedPage<?> that = (SeekedPage<?>) o;
        return hasNext == that.hasNext && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, hasNext);
    }

    @Override
    public String toString() {
        return "SeekedPage{" +
            "content=" + content +
            ", hasNext=" + hasNext +
            ", hasPrev=" + hasPrev +
            '}';
    }

    @SuppressWarnings("unchecked")
    public static <T> SeekedPage<T> empty() {
        return (SeekedPage<T>) EMPTY;
    }

    public static <T> SeekedPage<T> of(List<T> content, boolean hasNext, boolean hasPrev) {
        return new SeekedPage<>(content, hasNext, hasPrev);
    }

}
