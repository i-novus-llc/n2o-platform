package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.TailLengthDogFeature;

public interface TailLengthDogFeatureMapper extends Mapper<TailLengthDogFeature> {

    @SelectionKey("length")
    void setLength(TailLengthDogFeature feature);

}
