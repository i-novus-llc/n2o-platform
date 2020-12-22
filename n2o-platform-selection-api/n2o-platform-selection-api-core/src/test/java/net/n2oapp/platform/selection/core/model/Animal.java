package net.n2oapp.platform.selection.core.model;

import java.util.List;

public abstract class Animal {

    private String name;
    private double height;
    private String mother;
    private String father;
    private List<String> siblings;
    private List<Food> favoriteFood;

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

    public String getMother() {
        return mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }

    public String getFather() {
        return father;
    }

    public void setFather(String father) {
        this.father = father;
    }

    public List<String> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<String> siblings) {
        this.siblings = siblings;
    }

    public List<Food> getFavoriteFood() {
        return favoriteFood;
    }

    public void setFavoriteFood(List<Food> favoriteFood) {
        this.favoriteFood = favoriteFood;
    }

}
