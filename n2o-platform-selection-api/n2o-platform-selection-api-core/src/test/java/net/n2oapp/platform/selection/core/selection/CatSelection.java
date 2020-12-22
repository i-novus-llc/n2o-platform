package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.CatFeature;
import net.n2oapp.platform.selection.core.model.Dog;

public class CatSelection<E extends Cat> extends AnimalSelection<E, CatFeature<?>> {

    @SelectionKey("enemy")
    SelectionEnum selectEnemy;

    @SelectionKey("enemy")
    DogSelection<Dog> dogSelection;

    public SelectionEnum getSelectEnemy() {
        return selectEnemy;
    }

    public void setSelectEnemy(SelectionEnum selectEnemy) {
        this.selectEnemy = selectEnemy;
    }

    public DogSelection<Dog> getDogSelection() {
        return dogSelection;
    }

    public void setDogSelection(DogSelection<Dog> dogSelection) {
        this.dogSelection = dogSelection;
    }

}
