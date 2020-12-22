package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.EyeColorCatFeature;

public interface EyeColorCatFeatureMapper extends Mapper<EyeColorCatFeature> {

    @SelectionKey("color")
    void setColor(EyeColorCatFeature feature);

}
