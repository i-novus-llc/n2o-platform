package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.Mapper;
import net.n2oapp.platform.selection.api.NeedSelection;
import net.n2oapp.platform.selection.api.Selection;

@NeedSelection
public abstract class Test1<C, M> {

    static abstract class Test1Selection<E extends Test1<C, M>, C, M> implements Selection<E> {
    }

    static abstract class Test1Mapper<E extends Test1<C, M>, C, M> implements Mapper<E> {

    }

}
