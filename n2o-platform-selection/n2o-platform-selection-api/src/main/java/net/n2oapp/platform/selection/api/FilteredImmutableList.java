package net.n2oapp.platform.selection.api;

import java.util.*;

class FilteredImmutableList<E> extends AbstractList<E> {

    private final boolean[] filtered;
    private final List<E> src;
    private final int size;

    FilteredImmutableList(final boolean[] filtered, final List<E> src) {
        if (filtered.length != src.size())
            throw new IllegalArgumentException();
        this.filtered = filtered;
        this.src = src;
        int s = 0;
        for (int i = 0; i < filtered.length; i++) {
            if (!filtered[i])
                s++;
        }
        size = s;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(final Object o) {
        final int idx = src.indexOf(o);
        if (idx == -1)
            return false;
        return filtered[idx];
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private int idx = 0;
            {next0();}

            @Override
            public boolean hasNext() {
                return idx < src.size();
            }

            @Override
            public E next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                E res = src.get(idx++);
                next0();
                return res;
            }

            private void next0() {
                final int sz = src.size();
                while (idx < sz) {
                    if (!filtered[idx])
                        break;
                    idx++;
                }
            }

        };
    }

    @Override
    public E get(final int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(index);
        for (int i = 0, j = -1; i < src.size(); i++) {
            if (!filtered[i]) {
                j++;
                if (j == index)
                    return src.get(i);
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public int indexOf(final Object o) {
        for (int i = 0, j = -1; i < filtered.length; i++) {
            if (!filtered[i]) {
                j++;
                final E e = src.get(i);
                if (Objects.equals(e, o))
                    return j;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(final Object o) {
        for (int i = filtered.length - 1, j = size; i >= 0; i--) {
            if (!filtered[i]) {
                j--;
                final E e = src.get(i);
                if (Objects.equals(e, o))
                    return j;
            }
        }
        return -1;
    }

}
