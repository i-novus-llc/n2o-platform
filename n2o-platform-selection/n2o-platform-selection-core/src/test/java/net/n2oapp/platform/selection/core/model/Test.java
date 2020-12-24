package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.NeedSelection;

import java.io.Serializable;

@NeedSelection
public abstract class Test<A extends Test<A, E, T, ?> & Serializable, E extends java.lang.Object&java.util.Map<E,T>&java.lang.Iterable<T>, T extends E, C extends Test<?, ?, ?, ?>> {
}
