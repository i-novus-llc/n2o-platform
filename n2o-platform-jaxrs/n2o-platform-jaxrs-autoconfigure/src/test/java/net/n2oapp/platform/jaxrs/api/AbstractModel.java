package net.n2oapp.platform.jaxrs.api;

import javax.validation.constraints.NotNull;

public abstract class AbstractModel<T> {

    public AbstractModel() {
    }

    public AbstractModel(T value) {
        this.value = value;
    }

    @NotNull
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
