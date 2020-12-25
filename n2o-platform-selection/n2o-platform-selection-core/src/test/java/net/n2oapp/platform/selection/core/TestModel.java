package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.Selective;

import java.util.List;

public class TestModel {

    @Selective
    static class Test1<E extends Number, C> {
        Test2<?> test2;
    }

    interface Test1Selection<A extends Test1<E, C>, E extends Number, C> extends Selection<A> {
        Test2Selection<Test2<?>, ?> test2Selection();
    }

    @Selective
    static class Test2<B extends Test1<Integer, String>> extends Test1<Double, Character> {
        B test1;
        List<? extends B> list;
    }

    interface Test2Selection<A extends Test2<B>, B extends Test1<Integer, String>> extends Test1Selection<A, Double, Character> {
        Test1Selection<B, Integer, String> test1Selection();
        Test1Selection<? extends B, Integer, String> listSelection();
    }

    @Selective
    static class Test3<C extends Short> extends Test1<C, String> {
        Test1<C, C> test1;
    }

    interface Test3Selection<C extends Short> extends Test1Selection<Test3<C>, C, String> {
        Test1Selection<Test1<C, C>, C, C> test1Selection();
    }

}
