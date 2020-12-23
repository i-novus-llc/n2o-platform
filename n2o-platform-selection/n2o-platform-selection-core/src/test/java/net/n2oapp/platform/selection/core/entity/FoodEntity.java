package net.n2oapp.platform.selection.core.entity;

import net.n2oapp.platform.selection.core.mapper.FoodMapper;
import net.n2oapp.platform.selection.core.model.ColorEnum;
import net.n2oapp.platform.selection.core.model.Food;

public abstract class FoodEntity<M extends Food> implements FoodMapper<M> {

    private final String name;
    private final ColorEnum color;

    protected FoodEntity(String name, ColorEnum color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ColorEnum getColor() {
        return color;
    }

    @Override
    public void setName(M model) {
        model.setName(name);
    }

    @Override
    public void setColor(M model) {
        model.setColor(color);
    }

}
