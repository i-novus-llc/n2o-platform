package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

import java.io.Serializable;
import java.util.List;

/**
 * To test the compilation of the generated code
 */
public class TestModel {

    @Selective
    static class Test1<E extends Number, C> {
        Test2<?> test2;
        int i;
        int j;
    }

    @Selective
    static abstract class Test2<B extends Test1> extends Test1<Double, Character> {
        B test1;
        List<? extends B> list;
        List<? extends Test4> test4s;
    }

    @Selective(prefix = "t3")
    static class Test3<C extends Short> extends Test1<C, String> {
        Test1<C, ? extends Character> test1;
    }

    @Selective
    static class Test4<B extends Test6<? extends Integer, ? extends Integer, ?>> implements Serializable {
        B test6;
        List<B> test6s;
        List<? extends B> list;
    }
    @Selective
    static class Test5<B extends Test4<Test6<C, C, ?>>, C extends Integer> extends Test4<Test6<C, C, ?>> {
        B b1;
        List<B> bs;
    }

    @Selective
    static class Test10<B extends Test6<Integer, Integer, Test7<Integer>>> {
        Test4<Test6<Integer, Integer, Test7<Integer>>> test4;
        Test4<B> integerTest4;
        List<B> l;
        B b2;
    }

    @Selective
    static class Test9 extends Test10<Test6<Integer, Integer, Test7<Integer>>> {
    }

    @Selective
    static class Test6<C extends Integer, I extends C, E extends Test7<I>> extends Test4<Test6<C, I, E>> {
        Test4 fsdfdsffsdf;
        E sdfsdfsdf546;
        Test7<I> sdfsdfsf321;
    }

    @Selective
    static class Test7<E extends Integer> extends Test6<Integer, E, Test7<E>> {
    }

    @Selective
    static class Test13 extends Test7<Integer> {
    }

    @Selective
    static class Test14<C extends Integer, I extends C, E extends Test15<I, E>> {
        Test15<I, E> test15;
    }

    @Selective
    static class Test15<E extends Integer, I extends Test15<E, I>> extends Test14<E, E, I> {
    }

}
