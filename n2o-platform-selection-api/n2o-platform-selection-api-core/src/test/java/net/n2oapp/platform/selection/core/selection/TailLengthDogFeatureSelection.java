package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.TailLengthDogFeature;

public interface TailLengthDogFeatureSelection extends Selection<TailLengthDogFeature> {

    @SelectionKey("length")
    SelectionEnum selectLength();

}
