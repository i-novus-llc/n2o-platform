package net.n2oapp.platform.seek;

import java.io.Serializable;
import java.util.Objects;

public class SeekPivot implements Serializable {

    private final String name;
    private final String lastValue;

    public SeekPivot(String name, String lastValue) {
        this.name = name;
        this.lastValue = lastValue;
    }

    public String getName() {
        return name;
    }

    public String getLastValue() {
        return lastValue;
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

}
