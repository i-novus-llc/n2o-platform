package net.n2oapp.platform.selection.api;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

    @Test
    public void testEncodeDecode() {
        String json = "{\"name123\":\"T\",\"321positionSelection0321\":{\"b77\":\"F\"}}";
        String encode = Util.encode(json);
        String decode = Util.decode(encode);
        assertEquals(json, decode);
    }

}