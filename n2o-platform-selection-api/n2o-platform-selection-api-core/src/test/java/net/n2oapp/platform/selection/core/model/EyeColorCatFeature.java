package net.n2oapp.platform.selection.core.model;

public class EyeColorCatFeature implements CatFeature {

    private ColorEnum color;

    @Override
    public String name() {
        return "eye-color";
    }

    public ColorEnum color() {
        return color;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
    }

}
