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

    private Util() {
        throw new UnsupportedOperationException();
    }

}
