package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.DogFood;

public interface DogFoodSelection extends FoodSelection<DogFood> {

    @SelectionKey("type")
    SelectionEnum selectType();

    @SelectionKey("cost")
    SelectionEnum selectCost();

}
