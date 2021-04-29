package net.n2oapp.platform.selection.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Кодирование спец. символов JSON для передачи в параметрах запроса без кодирования процентами (url-encoding).
 */
final class Util {

    /**
     * Закодированный JSON начинается с этой строки
     */
    private static final String MAGIC = "7";

    static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//      Сортировка полей дает нам постоянное (то есть не меняющееся от реализации к реализации)
//      представление JSON у двух логически одинаковых выборок.
//      Это позволяет сохранить правильную работу web-cache-ей.
        MAPPER.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

//  Данные символы необязательно кодировать процентами.
    private static final char LPAREN    = '0'; // Символ '{'
    private static final char RPAREN    = '1'; // Символ '}'
    /**
     * Так как дефис не может присутствовать в валидном Java идентификаторе (в отличие от цифр) --
     * его можно использовать для кодирования двойных кавычек в JSON.
     */
    private static final char QUOTE     = '-'; // Символ '"'
    private static final char COLON     = '3'; // Символ ':'
    private static final char COMMA     = '4'; // Символ ','
//  ----------------------------------------------------

    private Util() {
        throw new UnsupportedOperationException();
    }

    static String encode(String json) {
        if (json == null)
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(MAGIC);
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
            } else if (c == ',') {
                builder.append(COMMA);
            } else
                builder.append(c);
        }
        return builder.toString();
    }

    static String decode(String encodedJson) {
        if (!encodedJson.startsWith(MAGIC))
            return encodedJson;
        StringBuilder decodedJson = new StringBuilder();
        boolean identifier = false;
        for (int i = MAGIC.length(); i < encodedJson.length(); i++) {
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
                } else if (c == COMMA) {
                    decodedJson.append(',');
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
