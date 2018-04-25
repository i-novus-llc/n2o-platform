package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;

/**
 * Модуль для {@link com.fasterxml.jackson.databind.ObjectMapper} позволяющий
 * серилизовать / десерилизовать базовые модели Spring Data, такие как Page, Pageable, Sort и т.п.
 */
public class SpringDataModule extends SimpleModule {

    public SpringDataModule()
    {
        super(PackageVersion.VERSION);
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Page.class, RestPage.class);
        resolver.addMapping(Pageable.class, RestCriteria.class);
        this.setAbstractTypes(resolver);
        this.addSerializer(new PageSerializer());
        this.addSerializer(new SortOrderSerializer());
        this.addDeserializer(Sort.class, new SortDeserializer());
    }

    static class PageSerializer extends StdSerializer<Page> {

        PageSerializer() {
            super(Page.class);
        }

        @Override
        public void serialize(Page value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("content", value.getContent());
            gen.writeNumberField("totalElements", value.getTotalElements());
            if (value.getSort() != null)
                gen.writeObjectField("sort", value.getSort());
            gen.writeEndObject();
        }
    }

    static class SortOrderSerializer extends StdSerializer<Sort.Order> {

        SortOrderSerializer() {
            super(Sort.Order.class);
        }

        @Override
        public void serialize(Sort.Order value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("property", value.getProperty());
            gen.writeStringField("direction", value.getDirection().name().toLowerCase());
            gen.writeEndObject();
        }
    }

    static class SortDeserializer extends JsonDeserializer<Sort> {

        @Override
        public Sort deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            ArrayNode node = jp.getCodec().readTree(jp);
            Sort.Order[] orders = new Sort.Order[node.size()];
            int i = 0;
            for (JsonNode obj : node) {
                Sort.Direction direction = obj.get("direction") != null ?
                        Sort.Direction.valueOf(obj.get("direction").asText().toUpperCase()) : null;
                orders[i] = new Sort.Order(direction, obj.get("property").asText());
                i++;
            }
            return new Sort(orders);
        }
    }
}
