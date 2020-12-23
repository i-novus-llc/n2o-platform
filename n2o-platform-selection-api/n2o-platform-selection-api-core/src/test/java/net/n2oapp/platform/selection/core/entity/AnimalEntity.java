package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.mapper.AnimalMapper;
import net.n2oapp.platform.selection.core.model.Animal;
import net.n2oapp.platform.selection.core.model.AnimalFeature;
import net.n2oapp.platform.selection.core.model.Food;

import java.util.List;

public abstract class AnimalEntity<M extends Animal<F>, F extends AnimalFeature> implements AnimalMapper<M, F> {

    private final String name;
    private final double height;

    private final String mother;
    private final String father;
    private final List<String> siblings;

    private List<FoodEntity<?>> favoriteFoods;

    protected AnimalEntity(String name, double height, String mother, String father, List<String> siblings) {
        this.name = name;
        this.height = height;
        this.mother = mother;
        this.father = father;
        this.siblings = siblings;
    }

    public double height() {
        return height;
    }

    public String name() {
        return name;
    }

    public String mother() {
        return mother;
    }

    public String father() {
        return father;
    }

    public List<String> siblings() {
        return siblings;
    }

    @Override
    public void setName(M m) {
        m.setName(name);
    }

    @Override
    public void setHeight(M m) {
        m.setHeight(height);
    }

    @Override
    public void setMother(M m) {
        m.setMother(mother);
    }

    @Override
    public void setFather(M m) {
        m.setFather(father);
    }

    @Override
    public void setSiblings(M m) {
        m.setSiblings(siblings);
    }

    @Override
    public List<? extends Mapper<? extends Food>> foodMappers() {
        return favoriteFoods;
    }

    public void setFavoriteFoods(List<FoodEntity<?>> favoriteFoods) {
        this.favoriteFoods = favoriteFoods;
    }

    @Override
    public void setFavoriteFood(M m, List<Food> favoriteFood) {
        m.setFavoriteFood(favoriteFood);
    }

    @Override
    public void setFeatures(M model, List<F> features) {
        model.setFeatures(features);
    }

}
