package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.mapper.DogMapper;
import net.n2oapp.platform.selection.core.model.Dog;

import java.util.List;

public class DogEntity extends AnimalEntity<DogEntity, Dog> implements DogMapper {

    public DogEntity(String name, double height, DogEntity mother, DogEntity father, List<DogEntity> siblings) {
        super(name, height, mother, father, siblings);
    }

    @Override
    public Dog create() {
        return new Dog();
    }

}
