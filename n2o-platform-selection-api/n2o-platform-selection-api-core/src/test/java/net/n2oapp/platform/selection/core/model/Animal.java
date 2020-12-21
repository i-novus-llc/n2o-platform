package net.n2oapp.platform.selection.core.model;

import java.util.List;

public abstract class Animal<T extends Animal<T>> {

    private String name;
    private double height;
    private T mother;
    private T father;
    private List<T> siblings;
    private Animal<?> enemy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public T getMother() {
        return mother;
    }

    public void setMother(T mother) {
        this.mother = mother;
    }

    public T getFather() {
        return father;
    }

    public void setFather(T father) {
        this.father = father;
    }

    public List<T> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<T> siblings) {
        this.siblings = siblings;
    }

    public Animal<?> getEnemy() {
        return enemy;
    }

    public void setEnemy(Animal<?> enemy) {
        this.enemy = enemy;
    }

}
