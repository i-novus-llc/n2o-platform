package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionEnum;
import net.n2oapp.platform.selection.api.SelectionKey;
import net.n2oapp.platform.selection.core.model.Food;

public abstract class FoodSelection<E extends Food> implements Selection<E> {

    @SelectionKey("name")
    SelectionEnum selectName;

    @SelectionKey("color")
    SelectionEnum selectColor;

    public SelectionEnum getSelectName() {
        return selectName;
    }

    public void setSelectName(SelectionEnum selectName) {
        this.selectName = selectName;
    }

    public SelectionEnum getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(SelectionEnum selectColor) {
        this.selectColor = selectColor;
    }

}
