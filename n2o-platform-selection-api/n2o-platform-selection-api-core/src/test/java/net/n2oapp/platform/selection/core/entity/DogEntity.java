package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.model.Dog;

import java.util.List;

public class DogEntity extends AnimalEntity<DogEntity, CatEntity, Dog> {

    public DogEntity(String name, double height, CatEntity enemy, DogEntity mother, DogEntity father, List<DogEntity> siblings) {
        super(name, height, mother, father, siblings, enemy);
    }

    @Override
    public Dog create() {
        return new Dog();
    }

}
