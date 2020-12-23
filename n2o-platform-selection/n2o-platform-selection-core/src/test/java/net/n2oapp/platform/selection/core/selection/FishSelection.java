package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.api.SelectionEnum;
import net.n2oapp.platform.selection.api.SelectionKey;
import net.n2oapp.platform.selection.core.model.Fish;

public class FishSelection extends FoodSelection<Fish> {

    @SelectionKey("habitats")
    SelectionEnum selectHabitats;

    public SelectionEnum getSelectHabitats() {
        return selectHabitats;
    }

    public void setSelectHabitats(SelectionEnum selectHabitats) {
        this.selectHabitats = selectHabitats;
    }

}
