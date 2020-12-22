package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.CatFeature;

public interface CatSelection extends AnimalSelection<Cat, CatFeature<?>> {

    @SelectionKey("enemy")
    SelectionEnum selectEnemy();

    @SelectionKey("enemy")
    DogSelection dogSelection();

}
