package net.n2oapp.platform.jaxrs.api;

public abstract class AbstractModel<T> {

    public AbstractModel() {
    }

    public AbstractModel(T value) {
        this.value = value;
    }

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
