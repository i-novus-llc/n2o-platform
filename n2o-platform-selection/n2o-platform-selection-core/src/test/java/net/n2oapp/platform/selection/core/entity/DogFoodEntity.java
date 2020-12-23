package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.mapper.DogFoodMapper;
import net.n2oapp.platform.selection.core.model.ColorEnum;
import net.n2oapp.platform.selection.core.model.DogFood;
import net.n2oapp.platform.selection.core.model.DogFoodEnum;

public class DogFoodEntity extends FoodEntity<DogFood> implements DogFoodMapper {

    private final int cost;
    private final DogFoodEnum type;

    public DogFoodEntity(String name, ColorEnum color, int cost, DogFoodEnum type) {
        super(name, color);
        this.cost = cost;
        this.type = type;
    }

    @Override
    public DogFood create() {
        return new DogFood();
    }

    @Override
    public void setType(DogFood food) {
        food.setType(type);
    }

    @Override
    public void setCost(DogFood food) {
        food.setCost(cost);
    }

}
