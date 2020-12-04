package net.n2oapp.platform.jaxrs.impl;

import net.n2oapp.platform.jaxrs.seek.SeekPivot;
import net.n2oapp.platform.jaxrs.seek.SeekPivotParameterConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeekPivotParameterConverterTest {

    private final SeekPivotParameterConverter converter = new SeekPivotParameterConverter();

    @Test
    void testConvert() {
        SeekPivot pivot = converter.fromString("id:5");
        assertEquals("id", pivot.getName());
        assertEquals("5", pivot.getLastValue());
        pivot = new SeekPivot(":\\na\\\\m\\:::e", "5");
        String s = converter.toString(pivot);
        SeekPivot parsed = converter.fromString(s);
        assertEquals(pivot, parsed);
    }

}