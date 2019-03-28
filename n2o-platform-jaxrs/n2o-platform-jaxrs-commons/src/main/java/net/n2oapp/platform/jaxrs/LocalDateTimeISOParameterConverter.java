package net.n2oapp.platform.jaxrs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Конвертер строки в формате ISO '2011-12-03T10:15:30' в объект LocalDateTime и обратно.
 */
public class LocalDateTimeISOParameterConverter implements TypedParamConverter<LocalDateTime> {

    @Override
    public Class<LocalDateTime> getType() {
        return LocalDateTime.class;
    }

    @Override
    public LocalDateTime fromString(String value) {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString(LocalDateTime value) {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
