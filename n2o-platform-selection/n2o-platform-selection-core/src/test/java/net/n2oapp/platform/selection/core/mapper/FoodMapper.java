package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.api.Mapper;
import net.n2oapp.platform.selection.api.SelectionKey;
import net.n2oapp.platform.selection.core.model.Food;

public interface FoodMapper<E extends Food> extends Mapper<E> {

    @SelectionKey("name")
    void setName(E model);

    @SelectionKey("color")
    void setColor(E model);

}
