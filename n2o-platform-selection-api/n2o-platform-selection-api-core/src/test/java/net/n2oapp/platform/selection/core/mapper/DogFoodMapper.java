package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.DogFood;

public interface DogFoodMapper extends FoodMapper<DogFood> {

    @SelectionKey("type")
    void setType(DogFood food);

    @SelectionKey("cost")
    void setCost(DogFood food);

}
