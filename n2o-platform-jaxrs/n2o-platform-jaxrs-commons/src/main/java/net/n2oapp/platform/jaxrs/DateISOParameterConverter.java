package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Конвертация {@link java.util.Date} в формате ISO8601
 */
public class DateISOParameterConverter implements TypedParamConverter<Date> {

    private static final String DATE_FORMAT_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    @Override
    public Date fromString(String value) {
        try {
            return ISO8601Utils.parse(value, new ParsePosition(0));
        } catch (ParseException e) {
            // in the case when requests come from feign clients
            try {
                return new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH).parse(value);
            } catch (ParseException e1) {
                throw new IllegalArgumentException("Date [" + value + "] doesn't have ISO format.");
            }
        }
    }

    @Override
    public String toString(Date value) {
        return ISO8601Utils.format(value);
    }
}
