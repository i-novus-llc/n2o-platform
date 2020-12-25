package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.Selective;

import java.util.List;

public class TestModel {

    @Selective
    static class Test1<E extends Number, C> {
        Test2<?> test2;
    }

    static class Test1Selection<A extends Test1<E, C>, E extends Number, C> implements Selection<A> {
        Test2Selection<?> test2Selection;
    }

    @Selective
    static class Test2<S extends Test1<Integer, String>> extends Test1<Double, Character> {
        S test1;
        List<? extends S> list;
    }

    static class Test2Selection<S extends Test1<Integer, String>> extends Test1Selection<Test2<S>, Double, Character> {
        Test1Selection<S, Integer, String> test1Selection;
        Test1Selection<? extends S, Integer, String> listSelection;
    }

}
