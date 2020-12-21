package net.n2oapp.platform.selection.core.model;

public class Dog extends Animal<Dog> {

    private Cat enemy;

    public Cat getEnemy() {
        return enemy;
    }

    public void setEnemy(Cat enemy) {
        this.enemy = enemy;
    }

}
