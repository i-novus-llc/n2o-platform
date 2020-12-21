package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;

public interface WithNameMapper<E> extends Mapper<E> {

    @SelectionKey("name")
    void setName(E model);

}
