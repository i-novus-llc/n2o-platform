package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.api.SelectionEnum;
import net.n2oapp.platform.selection.api.SelectionKey;
import net.n2oapp.platform.selection.core.model.DogFood;

public class DogFoodSelection extends FoodSelection<DogFood> {

    @SelectionKey("type")
    SelectionEnum selectType;

    @SelectionKey("cost")
    SelectionEnum selectCost;

    public SelectionEnum getSelectType() {
        return selectType;
    }

    public void setSelectType(SelectionEnum selectType) {
        this.selectType = selectType;
    }

    public SelectionEnum getSelectCost() {
        return selectCost;
    }

    public void setSelectCost(SelectionEnum selectCost) {
        this.selectCost = selectCost;
    }

}
