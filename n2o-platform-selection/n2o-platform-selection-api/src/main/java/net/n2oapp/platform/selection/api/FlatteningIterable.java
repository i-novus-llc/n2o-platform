package net.n2oapp.platform.selection.api;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatteningIterable<E> implements Iterable<E> {

    private final Iterable<? extends Iterable<E>> iterables;

    public FlatteningIterable(final Iterable<? extends Iterable<E>> iterables) {
        this.iterables = iterables;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private final Iterator<? extends Iterable<E>> iterablesIter = iterables.iterator();
            private Iterator<E> elementIter = next0();

            private Iterator<E> next0() {
                while (iterablesIter.hasNext()) {
                    final Iterable<E> next = iterablesIter.next();
                    final Iterator<E> iter = next.iterator();
                    if (iter.hasNext()) {
                        return iter;
                    }
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return elementIter != null;
            }

            @Override
            public E next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                final E res = elementIter.next();
                if (!elementIter.hasNext()) {
                    elementIter = next0();
                }
                return res;
            }

        };
    }

}
