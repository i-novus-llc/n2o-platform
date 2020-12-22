package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Fish;

public interface FishSelection extends FoodSelection<Fish> {

    @SelectionKey("habitats")
    SelectionEnum selectHabitats();

}
