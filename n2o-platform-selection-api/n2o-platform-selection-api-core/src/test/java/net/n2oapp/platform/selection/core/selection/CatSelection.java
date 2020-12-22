package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Cat;

public interface CatSelection extends AnimalSelection<Cat> {

    @SelectionKey("enemy")
    SelectionEnum selectEnemy();

    @SelectionKey("enemy")
    DogSelection dogSelection();

}
