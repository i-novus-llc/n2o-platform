package net.n2oapp.platform.selection.core.model;

public class Cat extends Animal<Cat> {

    private Dog enemy;

    public Dog getEnemy() {
        return enemy;
    }

    public void setEnemy(Dog enemy) {
        this.enemy = enemy;
    }

}
