package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;

public interface WithHeightMapper<E> extends Mapper<E> {

    @SelectionKey("height")
    void setHeight(E model);

}
