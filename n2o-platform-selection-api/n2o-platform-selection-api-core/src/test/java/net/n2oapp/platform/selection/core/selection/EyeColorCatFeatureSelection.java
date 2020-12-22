package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.EyeColorCatFeature;

public interface EyeColorCatFeatureSelection extends Selection<EyeColorCatFeature> {

    @SelectionKey("color")
    SelectionEnum selectColor();

}
