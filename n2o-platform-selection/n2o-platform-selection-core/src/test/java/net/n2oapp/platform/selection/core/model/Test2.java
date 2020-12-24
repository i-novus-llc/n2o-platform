package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.NeedSelection;
import net.n2oapp.platform.selection.api.Selection;

@NeedSelection
public class Test2 extends Test1<Integer, String> {

    static class Test2Selection extends Test1Selection<Test2, Integer, String> {
    }

    @NeedSelection
    static class Test3<C, E> extends Test2 {
    }

    static class Test3Selection<C, E> extends Test2Selection {
    }

    @NeedSelection
    static class Test4 {
    }

    static class Test4Selection implements Selection<Test4> {
    }

    @NeedSelection
    static abstract class Test5<C, M> extends Test1<C, M> {
    }

    static abstract class Test5Selection<E extends Test5<C, M>, C, M> extends Test1Selection<E, C, M> {
    }

    @NeedSelection
    static class Test6<C, M> {
    }

    static class Test6Selection<C, M> implements Selection<Test6<C, M>> {
    }

    @NeedSelection
    static class Test7 extends Test4 {
    }

    static class Test7Selection extends Test4Selection {
    }

    @NeedSelection
    static abstract class Test8<C, M> extends Test7 {
    }

    static abstract class Test8Selection<E extends Test8<C, M>, C, M> extends Test7Selection {

    }

    @NeedSelection
    static class Test9<C extends Integer, E extends String> {
    }

    static class Test9Selection<E extends Test9<C, I>, C extends Integer, I extends String> implements Selection<E> {
    }

    static class Test10<C extends Integer, E extends String> extends Test9<C, E> {
    }

    static class Test10Selection<C extends Integer, E extends String> extends Test9Selection<Test10<C, E>, C, E> {

    }



}
