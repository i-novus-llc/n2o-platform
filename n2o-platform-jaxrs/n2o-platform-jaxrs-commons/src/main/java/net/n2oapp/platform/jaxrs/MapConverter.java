package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Конвертер для Map
 */
public class MapConverter implements TypedParamConverter<Map> {

    private JavaType type;
    private ObjectMapper mapper;

    public MapConverter(Type genericType) {
       this(genericType, null);
    }

    public MapConverter(Type genericType, ObjectMapper mapper) {
        Class valueClass = null;

        if (genericType instanceof ParameterizedType &&
                ((ParameterizedType) genericType).getActualTypeArguments().length >= 2 &&
                ((ParameterizedType) genericType).getActualTypeArguments()[1] instanceof Class)
            valueClass = (Class)((ParameterizedType) genericType).getActualTypeArguments()[1];
        else
            valueClass = Object.class;

        if (mapper != null) {
            this.mapper = mapper;
        } else {
            this.mapper = new ObjectMapper();
        }
        this.type = this.mapper.getTypeFactory().constructMapType(HashMap.class, String.class, valueClass);
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
