package net.n2oapp.platform.selection.core.mapper;

import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.Dog;

public interface CatMapper extends AnimalMapper<Cat> {

    @SelectionKey("enemy")
    AnimalMapper<Dog> dogMapper();

    @SelectionKey("enemy")
    void setDog(Cat cat, Dog enemy);

}
