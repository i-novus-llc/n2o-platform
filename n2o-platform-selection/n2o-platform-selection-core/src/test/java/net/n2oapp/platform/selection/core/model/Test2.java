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
    static class Test9 extends Test1 {
    }

    static class Test9Selection<E extends Test1<Object, Object>> extends Test1Selection<E, Object, Object> {

    }

}
