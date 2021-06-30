package net.n2oapp.platform.selection.api;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class UtilTest {

    @Test
    public void testEncodeDecode() {
        String json = "{\"name123\":\"T\",\"321positionSelection0321\":{\"b77\":\"F\"}}";
        String urlBase = "http://rest-api.ru?selection=";
        String encode = Util.encode(json);
        String decode = Util.decode(encode);
        assertEquals(json, decode);
        String uri = URI.create(urlBase + encode).toString();
        assertEquals(json.length() + urlBase.length() + 1, uri.length());
    }

}