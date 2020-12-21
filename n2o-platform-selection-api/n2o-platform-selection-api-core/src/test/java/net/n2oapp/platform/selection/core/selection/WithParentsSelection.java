package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;

public interface WithParentsSelection<E> extends Selection<E> {

    @SelectionKey("mother")
    SelectionEnum fetchMother();

    @SelectionKey("mother")
    Selection<E> motherSelection();

    @SelectionKey("father")
    SelectionEnum fetchFather();

    @SelectionKey("father")
    Selection<E> fatherSelection();

}
