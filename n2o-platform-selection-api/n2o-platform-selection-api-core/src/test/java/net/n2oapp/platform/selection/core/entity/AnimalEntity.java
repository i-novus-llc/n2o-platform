package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.mapper.AnimalMapper;
import net.n2oapp.platform.selection.core.mapper.FoodMapper;
import net.n2oapp.platform.selection.core.model.Animal;
import net.n2oapp.platform.selection.core.model.Food;

import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class AnimalEntity<T extends AnimalEntity<T, M>, M extends Animal> implements AnimalMapper<M> {

    private final String name;
    private final double height;

    private final T mother;
    private final T father;
    private final List<T> siblings;

    private List<? extends FoodEntity<?>> favoriteFoods;

    protected AnimalEntity(String name, double height, T mother, T father, List<T> siblings) {
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

    public T mother() {
        return mother;
    }

    public T father() {
        return father;
    }

    public List<T> siblings() {
        return siblings;
    }

    @Override
    public void setHeight(M model) {
        model.setHeight(height);
    }

    @Override
    public void setName(M model) {
        model.setName(name);
    }

    @Override
    public void setMother(M model) {
        model.setMother(mother.name());
    }

    @Override
    public void setFather(M model) {
        model.setFather(father.name());
    }

    @Override
    public void setSiblings(M model) {
        model.setSiblings(siblings.stream().map(AnimalEntity::name).collect(toList()));
    }

    @Override
    public List<? extends FoodMapper<?>> foodMappers() {
        return favoriteFoods;
    }

    @Override
    public void setFavoriteFood(M m, List<Food> favoriteFood) {
        m.setFavoriteFood(favoriteFood);
    }

}
