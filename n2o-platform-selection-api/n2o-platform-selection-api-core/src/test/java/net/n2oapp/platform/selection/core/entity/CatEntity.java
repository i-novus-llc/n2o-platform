package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.mapper.AnimalMapper;
import net.n2oapp.platform.selection.core.mapper.CatMapper;
import net.n2oapp.platform.selection.core.model.Cat;
import net.n2oapp.platform.selection.core.model.Dog;

import java.util.List;

public class CatEntity extends AnimalEntity<CatEntity, Cat> implements CatMapper {

    private final DogEntity enemy;

    public CatEntity(String name, double height, CatEntity mother, CatEntity father, List<CatEntity> siblings, DogEntity enemy) {
        super(name, height, mother, father, siblings);
        this.enemy = enemy;
    }

    @Override
    public Cat create() {
        return new Cat();
    }

    @Override
    public AnimalMapper<Dog> dogMapper() {
        return enemy;
    }

    @Override
    public void setDog(Cat cat, Dog enemy) {
        cat.setEnemy(enemy);
    }

}
