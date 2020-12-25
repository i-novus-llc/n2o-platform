package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

import java.util.List;

public class TestModel {

    @Selective
    static class Test1<E extends Number, C> {
        Test2<?> test2;
    }

    @Selective
    static abstract class Test2<B extends Test1<Integer, String>> extends Test1<Double, Character> {
        B test1;
        List<? extends B> list;
        List<? extends Test4> test4s;
    }

    @Selective
    static class Test3<C extends Short> extends Test1<C, String> {
        Test1<C, ? extends Character> test1;
    }

    @Selective
    static class Test4 {
        Test5<? extends Integer> test5;
    }

    @Selective
    static class Test5<C extends Integer> extends Test4 {
        Test4 test4;
    }

}
