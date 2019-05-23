package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.jaxrs.TypedParamConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapConverter implements TypedParamConverter<Map> {

    private JavaType type;
    private ObjectMapper mapper;

    public MapConverter(Class valueClass) {
        this.mapper = new ObjectMapper();
        this.type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, valueClass);
    }

    @Override
    public Class<Map> getType() {
        return Map.class;
    }

    @Override
    public Map fromString(String value) {
        try {
            return mapper.readValue(value, type);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Failed to convert string '%s' to Map", value), e);
        }
    }

    @Override
    public String toString(Map value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert from Map to string", e);
        }
    }
}
