package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.NeedSelection;

@NeedSelection
public class Cat extends Animal<CatFeature> {

    private Dog enemy;

    public Dog getEnemy() {
        return enemy;
    }

    public void setEnemy(Dog enemy) {
        this.enemy = enemy;
    }

}
