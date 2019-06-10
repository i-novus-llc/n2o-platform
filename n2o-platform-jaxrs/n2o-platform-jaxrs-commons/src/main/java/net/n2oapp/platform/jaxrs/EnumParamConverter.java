package net.n2oapp.platform.jaxrs;

import java.util.Arrays;

/**
 * Типизрованный конвертер для Enum'ов
 * @param <T>
 */
public class EnumParamConverter<T extends Enum> implements TypedParamConverter<T> {

    private Class<T> type;

    public EnumParamConverter(Class<T> type) {
        this.type = type;
    }


    @Override
    public Class<T> getType() {
        return type;
    }


    @Override
    public T fromString(String value) {
        if (value == null) return null;
        return Arrays.stream(type.getEnumConstants())
                .filter(c -> value.equals(c.name()))
                .findAny().orElse(null);
    }

    @Override
    public String toString(T value) {
        if (value == null) return null;
        return value.name();
    }
}
