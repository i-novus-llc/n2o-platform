package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

/**
 * Конвертация {@link java.util.Date} в формате ISO8601
 */
public class DateISOParameterConverter implements TypedParamConverter<Date> {

    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    @Override
    public Date fromString(String value) {
        try {
            return ISO8601Utils.parse(value, new ParsePosition(0));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date [" + value + "] doesn't have ISO format.");
        }
    }

    @Override
    public String toString(Date value) {
        return ISO8601Utils.format(value);
    }
}
