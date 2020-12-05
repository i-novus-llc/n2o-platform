package net.n2oapp.platform.jaxrs.seek;

import java.io.Serializable;
import java.util.Objects;

public class SeekPivot implements Serializable {

    private final String name;
    private final String lastValue;

    private SeekPivot(String name, String lastValue) {
        this.name = name;
        this.lastValue = lastValue;
    }

    public String getName() {
        return name;
    }

    public String getLastValue() {
        return lastValue;
    }

    public SeekPivot copy() {
        return of(name, lastValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeekPivot)) return false;
        SeekPivot seekPivot = (SeekPivot) o;
        return Objects.equals(getName(), seekPivot.getName()) && Objects.equals(getLastValue(), seekPivot.getLastValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getLastValue());
    }

    public static SeekPivot of(String name, String lastValue) {
        return new SeekPivot(name, lastValue);
    }

}
