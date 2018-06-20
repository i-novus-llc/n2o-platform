package net.n2oapp.platform.jaxrs;

import java.time.ZonedDateTime;

/**
 * Конвертация {@link java.time.ZonedDateTime}
 */
public class ZonedDateTimeParamConverter implements TypedParamConverter<ZonedDateTime> {

    @Override
    public Class<ZonedDateTime> getType() {
        return ZonedDateTime.class;
    }

    @Override
    public ZonedDateTime fromString(String string) {
        return ZonedDateTime.parse(string);
    }

    @Override
    public String toString(ZonedDateTime t) {
        return t.toString();
    }
}
