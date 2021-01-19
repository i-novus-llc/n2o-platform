package net.n2oapp.platform.jaxrs.seek;

import net.n2oapp.platform.jaxrs.TypedParamConverter;

public class SeekPivotParameterConverter implements TypedParamConverter<SeekPivot> {

    @Override
    public SeekPivot fromString(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ':') {
                return SeekPivot.of(value.substring(0, i), value.substring(i + 1));
            }
        }
        throw new IllegalArgumentException("No ':' can be found on '" + value + "'");
    }

    /**
     * Так как в валидном java-identifier не может присутствовать ':' -- его можно использовать в качестве разделителя
     */
    @Override
    public String toString(SeekPivot value) {
        return value.getName() + ":" + value.getLastValue();
    }

    @Override
    public Class<SeekPivot> getType() {
        return SeekPivot.class;
    }

}
