package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Fish;

public interface FishMapper extends FoodMapper<Fish> {

    @SelectionKey("habitats")
    void setHabitats(Fish fish);

}
