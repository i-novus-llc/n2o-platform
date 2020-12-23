package net.n2oapp.platform.selection.core.selection;

import net.n2oapp.platform.selection.core.Selection;
import net.n2oapp.platform.selection.core.SelectionEnum;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.CatFeature;
import net.n2oapp.platform.selection.core.model.Dog;

public class CatSelection extends AnimalSelection<Cat, CatFeature> {

    @SelectionKey("enemy")
    SelectionEnum selectEnemy;

    @SelectionKey("enemy")
    Selection<Dog> enemySelection;

    public SelectionEnum getSelectEnemy() {
        return selectEnemy;
    }

    public void setSelectEnemy(SelectionEnum selectEnemy) {
        this.selectEnemy = selectEnemy;
    }

    public Selection<Dog> getEnemySelection() {
        return enemySelection;
    }

    public void setEnemySelection(Selection<Dog> dogSelection) {
        this.enemySelection = dogSelection;
    }

}
