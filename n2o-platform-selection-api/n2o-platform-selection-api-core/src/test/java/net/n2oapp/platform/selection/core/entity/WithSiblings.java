package net.n2oapp.platform.selection.core.entity;

import java.util.List;

public interface WithSiblings<E extends WithSiblings<E>> {
    List<E> siblings();
}
