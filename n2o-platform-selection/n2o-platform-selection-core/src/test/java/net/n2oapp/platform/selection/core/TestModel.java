package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.NeedSelection;

public class TestModel {

    @NeedSelection
    static class Test1<E extends Number, C> {
        Test2<?> test2;
    }

    @NeedSelection
    static class Test2<S extends Test1<Integer, String>> extends Test1<Double, Character> {
        S test1;
    }

}
