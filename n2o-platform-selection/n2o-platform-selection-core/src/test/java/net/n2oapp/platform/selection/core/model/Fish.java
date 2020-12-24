package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.NeedSelection;

import java.util.List;

@NeedSelection
public class Fish extends Food {

    private List<Ocean> habitats;

    public List<Ocean> getHabitats() {
        return habitats;
    }

    public void setHabitats(List<Ocean> habitats) {
        this.habitats = habitats;
    }

}
