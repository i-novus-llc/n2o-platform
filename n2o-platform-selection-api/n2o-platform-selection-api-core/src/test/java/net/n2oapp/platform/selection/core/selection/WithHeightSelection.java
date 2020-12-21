package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;

public interface WithHeightSelection<E> extends Selection<E> {

    @SelectionKey("height")
    SelectionEnum fetchHeight();

}
