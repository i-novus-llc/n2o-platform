package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListConverter implements TypedParamConverter<List> {

    private JavaType type;
    private ObjectMapper mapper;

    public ListConverter(Class valueClass) {
        this.mapper = new ObjectMapper();
        JavaTimeModule jtm = new JavaTimeModule();
        mapper.registerModule(jtm);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());
        mapper.registerModule(simpleModule);
        this.type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, valueClass);
    }

    @Override
    public List fromString(String value) {
        try {
            return mapper.readValue(value, type);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Failed to convert string '%s' to List", value), e);
        }
    }

    @Override
    public String toString(List value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert from List to string", e);
        }
    }

    @Override
    public Class<List> getType() {
        return List.class;
    }

    public static class BigDecimalPlainSerializer extends JsonSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.stripTrailingZeros().toPlainString());
        }

        @Override
        public void serializeWithType(BigDecimal value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
            typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_STRING));
            serialize(value, gen, null);
            typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_STRING));
        }
    }
}
