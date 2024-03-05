package net.n2oapp.platform.jaxrs;

import jakarta.ws.rs.WebApplicationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Конвертация {@link java.util.Date} в предопределенном формате
 */
public class DateParameterConverter implements TypedParamConverter<Date> {

    private String format;

    /**
     * Конструктор конвертатора
     * @param format Формат конвертации
     */
    public DateParameterConverter(String format) {
        this.format = format;
    }

    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    @Override
    public Date fromString(String string) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.parse(string);
        } catch (ParseException ex) {
            throw new WebApplicationException(ex);
        }
    }

    @Override
    public String toString(Date t) {
        return new SimpleDateFormat(format).format(t);
    }
}