package net.n2oapp.platform.selection.api;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FilteredImmutableListTest  {

    @Test
    public void test() {
        FilteredImmutableList<Integer> list = new FilteredImmutableList<>(
            new boolean[] {true, false, true},
            List.of(1, 2, 3)
        );
        assertEquals(1, list.size());
        assertEquals(2, ((int) list.get(0)));
        assertEquals(0, list.indexOf(2));
        assertEquals(-1, list.indexOf(1));
        assertEquals(-1, list.lastIndexOf(3));
        final Iterator<Integer> iter = list.iterator();
        assertEquals(2, ((int) iter.next()));
        assertFalse(iter.hasNext());
    }

}