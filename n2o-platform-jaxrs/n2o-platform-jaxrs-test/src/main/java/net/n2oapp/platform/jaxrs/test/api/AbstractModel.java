package net.n2oapp.platform.jaxrs.test.api;

public abstract class AbstractModel<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
