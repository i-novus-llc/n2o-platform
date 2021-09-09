package net.n2oapp.platform.selection.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FenwickTreeTest {

    @Test
    public void test() {
        final FenwickTree tree = new FenwickTree(6);
        tree.increment(5);
        assertEquals(0, tree.sum(0));
        assertEquals(0, tree.sum(1));
        assertEquals(0, tree.sum(2));
        assertEquals(0, tree.sum(3));
        assertEquals(0, tree.sum(4));
        assertEquals(1, tree.sum(5));
        tree.increment(3);
        assertEquals(0, tree.sum(0));
        assertEquals(0, tree.sum(1));
        assertEquals(0, tree.sum(2));
        assertEquals(1, tree.sum(3));
        assertEquals(1, tree.sum(4));
        assertEquals(2, tree.sum(5));
        tree.increment(0);
        assertEquals(1, tree.sum(0));
        assertEquals(1, tree.sum(1));
        assertEquals(1, tree.sum(2));
        assertEquals(2, tree.sum(3));
        assertEquals(2, tree.sum(4));
        assertEquals(3, tree.sum(5));
        tree.increment(2);
        assertEquals(1, tree.sum(0));
        assertEquals(1, tree.sum(1));
        assertEquals(2, tree.sum(2));
        assertEquals(3, tree.sum(3));
        assertEquals(3, tree.sum(4));
        assertEquals(4, tree.sum(5));
    }

}