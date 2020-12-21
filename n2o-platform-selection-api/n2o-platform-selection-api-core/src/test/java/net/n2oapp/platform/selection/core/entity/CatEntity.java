package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.model.Cat;

import java.util.List;

public class CatEntity extends AnimalEntity<CatEntity, DogEntity, Cat> {

    public CatEntity(String name, double height, DogEntity enemy, CatEntity mother, CatEntity father, List<CatEntity> siblings) {
        super(name, height, mother, father, siblings, enemy);
    }

    @Override
    public Cat create() {
        return new Cat();
    }

}
