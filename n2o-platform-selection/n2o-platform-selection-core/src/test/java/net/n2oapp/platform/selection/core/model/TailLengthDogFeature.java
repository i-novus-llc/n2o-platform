package net.n2oapp.platform.selection.core.model;

import net.n2oapp.platform.selection.api.NeedSelection;

@NeedSelection
public class TailLengthDogFeature implements DogFeature {

    private int length;

    @Override
    public String name() {
        return "tail-length";
    }

    public int length() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
