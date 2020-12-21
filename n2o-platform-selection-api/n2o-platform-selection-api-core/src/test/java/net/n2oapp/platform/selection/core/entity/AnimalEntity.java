package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.Mapper;
import net.n2oapp.platform.selection.core.SelectionKey;
import net.n2oapp.platform.selection.core.mapper.WithHeightMapper;
import net.n2oapp.platform.selection.core.mapper.WithNameMapper;
import net.n2oapp.platform.selection.core.mapper.WithParentsMapper;
import net.n2oapp.platform.selection.core.mapper.WithSiblingsMapper;
import net.n2oapp.platform.selection.core.model.Animal;

import java.util.List;

public abstract class AnimalEntity<T extends AnimalEntity<T, E, M>, E extends AnimalEntity<E, T, ? extends Animal<?>>, M extends Animal<M>> implements
        WithHeight,
        WithName,
        WithParents<T>,
        WithSiblings<T>,
        Mapper<M>,
        WithHeightMapper<M>,
        WithNameMapper<M>,
        WithParentsMapper<M>,
        WithSiblingsMapper<M> {

    private final String name;
    private final double height;

    private final T mother;
    private final T father;
    private final List<T> siblings;

    private final E enemy;

    protected AnimalEntity(String name, double height, T mother, T father, List<T> siblings, E enemy) {
        this.name = name;
        this.height = height;
        this.mother = mother;
        this.father = father;
        this.siblings = siblings;
        this.enemy = enemy;
    }

    @Override
    public double height() {
        return height;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public T mother() {
        return mother;
    }

    @Override
    public T father() {
        return father;
    }

    @Override
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
    public void setMother(M model, M mother) {
        model.setMother(mother);
    }

    @Override
    public Mapper<M> motherMapper() {
        return mother;
    }

    @Override
    public void setFather(M model, M father) {
        model.setFather(father);
    }

    @Override
    public Mapper<M> fatherMapper() {
        return father;
    }

    @Override
    public void setSiblings(M model, List<M> siblings) {
        model.setSiblings(siblings);
    }

    @Override
    public List<? extends Mapper<M>> siblingMappers() {
        return siblings;
    }

    @SelectionKey("enemy")
    public Mapper<Animal<?>> enemyMapper() {
        return (Mapper<Animal<?>>) enemy;
    }

    @SelectionKey("enemy")
    public void setEnemy(M model, Animal<?> enemy) {
        model.setEnemy(enemy);
    }

}
