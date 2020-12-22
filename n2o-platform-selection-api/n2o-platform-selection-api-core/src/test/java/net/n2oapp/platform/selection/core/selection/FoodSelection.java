package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Food;

public interface FoodSelection<E extends Food> extends Selection<Food> {

    @SelectionKey("name")
    SelectionEnum selectName(E model);

    @SelectionKey("color")
    SelectionEnum selectColor(E model);

}
