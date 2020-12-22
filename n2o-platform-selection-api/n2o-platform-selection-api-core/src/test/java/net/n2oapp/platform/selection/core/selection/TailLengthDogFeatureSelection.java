package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.TailLengthDogFeature;

public class TailLengthDogFeatureSelection implements Selection<TailLengthDogFeature> {

    @SelectionKey("length")
    SelectionEnum selectLength;

    public SelectionEnum getSelectLength() {
        return selectLength;
    }

    public void setSelectLength(SelectionEnum selectLength) {
        this.selectLength = selectLength;
    }

}
