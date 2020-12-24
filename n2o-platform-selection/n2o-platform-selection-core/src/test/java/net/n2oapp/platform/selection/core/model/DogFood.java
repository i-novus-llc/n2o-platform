package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.NeedSelection;

@NeedSelection
public class DogFood extends Food {

    private DogFoodEnum type;
    private int cost;

    public DogFoodEnum getType() {
        return type;
    }

    public void setType(DogFoodEnum type) {
        this.type = type;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

}
