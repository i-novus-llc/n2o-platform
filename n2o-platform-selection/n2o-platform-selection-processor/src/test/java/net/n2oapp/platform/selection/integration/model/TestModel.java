package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.Selective;

import java.io.Serializable;
import java.util.List;

public class TestModel {
    @Selective
    static class Test1<E extends Number, C> {
        @Joined
        Test2 test2;
        int i;
        int j;

        public Test2 getTest2() {
            return test2;
        }

        public void setTest2(Test2 test2) {
            this.test2 = test2;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public int getJ() {
            return j;
        }

        public void setJ(int j) {
            this.j = j;
        }
    }

    @Selective
    static abstract class Test2 extends Test1<Double, Character> {
    }

    @Selective(prefix = "t3")
    static class Test3<C extends Short> extends Test1<C, String> {
        @Joined
        Test1<C, ? extends Character> test1;

        public Test1<C, ? extends Character> getTest1() {
            return test1;
        }

        public void setTest1(Test1<C, ? extends Character> test1) {
            this.test1 = test1;
        }
    }

    @Selective
    static class Test4<B extends Test6<? extends Integer, ? extends Integer, ?>> implements Serializable {
        @Joined
        B test6;
        @Joined
        List<B> test6s;
        @Joined
        List<? extends B> list;

        public B getTest6() {
            return test6;
        }

        public void setTest6(B test6) {
            this.test6 = test6;
        }

        public List<B> getTest6s() {
            return test6s;
        }

        public void setTest6s(List<B> test6s) {
            this.test6s = test6s;
        }

        public List<? extends B> getList() {
            return list;
        }

        public void setList(List<? extends B> list) {
            this.list = list;
        }
    }
    @Selective
    static class Test5<B extends Test4<Test6<C, C, ?>>, C extends Integer> extends Test4<Test6<C, C, ?>> {
        @Joined
        B b1;
        @Joined
        List<B> bs;

        public B getB1() {
            return b1;
        }

        public void setB1(B b1) {
            this.b1 = b1;
        }

        public List<B> getBs() {
            return bs;
        }

        public void setBs(List<B> bs) {
            this.bs = bs;
        }
    }

    @Selective
    static class Test9<B extends Test6<Integer, Integer, Test7<Integer>>> {
        @Joined
        Test4<Test6<Integer, Integer, Test7<Integer>>> test4;
        @Joined
        Test4<B> integerTest4;
        @Joined
        List<B> l;
        @Joined
        B b2;

        public Test4<Test6<Integer, Integer, Test7<Integer>>> getTest4() {
            return test4;
        }

        public void setTest4(Test4<Test6<Integer, Integer, Test7<Integer>>> test4) {
            this.test4 = test4;
        }

        public Test4<B> getIntegerTest4() {
            return integerTest4;
        }

        public void setIntegerTest4(Test4<B> integerTest4) {
            this.integerTest4 = integerTest4;
        }

        public List<B> getL() {
            return l;
        }

        public void setL(List<B> l) {
            this.l = l;
        }

        public B getB2() {
            return b2;
        }

        public void setB2(B b2) {
            this.b2 = b2;
        }
    }

    @Selective
    static class Test8 extends Test9<Test6<Integer, Integer, Test7<Integer>>> {
    }

    @Selective
    static class Test6<C extends Integer, I extends C, E extends Test7<I>> extends Test4<Test6<C, I, E>> {
        @Joined
        E sdfsdfsdf546;
        @Joined
        Test7<I> sdfsdfsf321;

        public E getSdfsdfsdf546() {
            return sdfsdfsdf546;
        }

        public void setSdfsdfsdf546(E sdfsdfsdf546) {
            this.sdfsdfsdf546 = sdfsdfsdf546;
        }

        public Test7<I> getSdfsdfsf321() {
            return sdfsdfsf321;
        }

        public void setSdfsdfsf321(Test7<I> sdfsdfsf321) {
            this.sdfsdfsf321 = sdfsdfsf321;
        }
    }

    @Selective
    static class Test7<E extends Integer> extends Test6<Integer, E, Test7<E>> {

    }

    @Selective
    static class Test10 extends Test7<Integer> {
    }

    @Selective
    static class Test11<C extends Integer, I extends C, E extends Test12<C>> {
        @Joined
        Test12<C> test12;

        public Test12<C> getTest12() {
            return test12;
        }

        public void setTest12(Test12<C> test12) {
            this.test12 = test12;
        }
    }

    @Selective
    static class Test12<E extends Integer> extends Test11<E, E, Test12<E>> {
    }

    @Selective
    static class Test13 extends Test11<Integer, Integer, Test12<Integer>> {

    }

}
