package net.n2oapp.platform.selection.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FlatteningIterableTest {

    @Test
    void testFlatten() {
        final List<List<Integer>> lists = List.of(
            List.of(1, 2, 3),
            Collections.emptyList(),
            List.of(3, 2),
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(1)
        );
        final FlatteningIterable<Integer> iterable = new FlatteningIterable<>(lists);
        Iterator<Integer> iter = iterable.iterator();
        assertEquals(1, ((int) iter.next()));
        assertEquals(2, ((int) iter.next()));
        assertEquals(3, ((int) iter.next()));
        assertEquals(3, ((int) iter.next()));
        assertEquals(2, ((int) iter.next()));
        assertEquals(1, ((int) iter.next()));
        assertFalse(iter.hasNext());
        iter = iterable.iterator();
        int sum = 0;
        while (iter.hasNext()) {
            sum += iter.next();
        }
        assertEquals(12, sum);
    }

}