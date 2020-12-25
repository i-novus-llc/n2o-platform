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
        Test2Selection<? extends Test2<?>, ?> test2Selection();
    }

    @Selective
    static abstract class Test2<B extends Test1<Integer, String>> extends Test1<Double, Character> {
        B test1;
        List<? extends B> list;
        List<? extends Test4> test4s;
    }

    interface Test2Selection<A extends Test2<B>, B extends Test1<Integer, String>> extends Test1Selection<A, Double, Character> {
        Test1Selection<B, Integer, String> test1Selection();
        Test1Selection<? extends B, Integer, String> listSelection();
        Test4Selection<? extends Test4> test4sSelection();
    }

    @Selective
    static class Test3<C extends Short> extends Test1<C, String> {
        Test1<C, ? extends Character> test1;
    }

    interface Test3Selection<C extends Short> extends Test1Selection<Test3<C>, C, String> {
        Test1Selection<? extends Test1<C, ? extends Character>, C, ? extends Character> test1Selection();
    }

    @Selective
    static class Test4 {
        Test5<? extends Integer> test5;
    }

    interface Test4Selection<A extends Test4> extends Selection<A> {
        Test5Selection<? extends Integer> test5Selection();
    }

    @Selective
    static class Test5<C extends Integer> extends Test4 {
        Test4 test4;
    }

    interface Test5Selection<C extends Integer> extends Test4Selection<Test5<C>> {
        Test4Selection<? extends Test4> test4Selection();
    }

}
