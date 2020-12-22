package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.mapper.FishMapper;
import net.n2oapp.platform.selection.core.model.ColorEnum;
import net.n2oapp.platform.selection.core.model.Fish;
import net.n2oapp.platform.selection.core.model.Ocean;

import java.util.List;

public class FishEntity extends FoodEntity<Fish> implements FishMapper {

    private final List<Ocean> habitats;

    public FishEntity(String name, ColorEnum color, List<Ocean> habitats) {
        super(name, color);
        this.habitats = habitats;
    }

    @Override
    public Fish create() {
        return new Fish();
    }

    @Override
    public void setHabitats(Fish fish) {
        fish.setHabitats(habitats);
    }

}
