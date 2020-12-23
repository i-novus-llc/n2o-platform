package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionEnum;
import net.n2oapp.platform.selection.api.SelectionKey;
import net.n2oapp.platform.selection.core.model.EyeColorCatFeature;

public class EyeColorCatFeatureSelection implements Selection<EyeColorCatFeature> {

    @SelectionKey("color")
    SelectionEnum selectColor;

    public SelectionEnum getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(SelectionEnum selectColor) {
        this.selectColor = selectColor;
    }

}
