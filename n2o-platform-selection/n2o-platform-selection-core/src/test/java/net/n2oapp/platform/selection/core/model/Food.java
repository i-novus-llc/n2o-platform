package net.n2oapp.platform.selection.core.model;

public abstract class Food {

    private String name;
    private ColorEnum color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColorEnum getColor() {
        return color;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
    }

}
