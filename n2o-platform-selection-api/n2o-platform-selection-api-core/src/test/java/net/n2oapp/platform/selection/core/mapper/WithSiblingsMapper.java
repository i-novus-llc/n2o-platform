package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;

import java.util.List;

public interface WithSiblingsMapper<E> extends Mapper<E> {

    @SelectionKey("siblings")
    List<? extends Mapper<E>> siblingMappers();

    @SelectionKey("siblings")
    void setSiblings(E model, List<E> siblings);

}
