package net.n2oapp.platform.jaxrs;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTimeISOParameterConverterTest {

    private final LocalDateTimeISOParameterConverter converter = new LocalDateTimeISOParameterConverter();

    @Test
    public void testConvert() {
        String date = "2019-09-20T17:35:22.262721111";
        LocalDateTime converted = converter.fromString(date);
        assertEquals(262721111L, converted.getNano());
    }

}