package net.n2oapp.platform.selection.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Util {

    static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY); // Ensures consistent json view of equal selections
    }

    private static final char LPAREN    = '0';
    private static final char RPAREN    = '1';
    private static final char QUOTE     = '-';
    private static final char COLON     = '3';

    private Util() {
        throw new UnsupportedOperationException();
    }

    static String encode(String json) {
        if (json == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                builder.append(LPAREN);
            } else if (c == '}') {
                builder.append(RPAREN);
            } else if (c == '"') {
                builder.append(QUOTE);
            } else if (c == ':') {
                builder.append(COLON);
            } else
                builder.append(c);
        }
        return builder.toString();
    }

    static String decode(String encodedJson) {
        if (encodedJson == null)
            return null;
        StringBuilder decodedJson = new StringBuilder();
        boolean identifier = false;
        for (int i = 0; i < encodedJson.length(); i++) {
            char c = encodedJson.charAt(i);
            if (!identifier) {
                if (c == LPAREN) {
                    decodedJson.append('{');
                } else if (c == RPAREN) {
                    decodedJson.append('}');
                } else if (c == QUOTE) {
                    identifier = true;
                    decodedJson.append('"');
                } else if (c == COLON) {
                    decodedJson.append(':');
                } else
                    decodedJson.append(c);
            } else {
                if (c == QUOTE) {
                    identifier = false;
                    decodedJson.append('"');
                } else
                    decodedJson.append(c);
            }
        }
        return decodedJson.toString();
    }

}
