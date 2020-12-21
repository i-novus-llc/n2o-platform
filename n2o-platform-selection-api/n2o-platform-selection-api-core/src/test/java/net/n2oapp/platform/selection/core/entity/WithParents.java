package net.n2oapp.platform.selection.core.entity;

public interface WithParents<E extends WithParents<E>> {

    E mother();
    E father();

}
