package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Модуль для {@link com.fasterxml.jackson.databind.ObjectMapper} позволяющий
 * серилизовать / десерилизовать базовые модели Spring Data, такие как Page, Pageable, Sort и т.п.
 */
public abstract class SpringDataModule extends SimpleModule {

    static final String DIRECTION = "direction";
    static final String PROPERTY = "property";
    static final String CONTENT = "content";
    static final String TOTAL_ELEMENTS = "totalElements";
    static final String SORT = "sort";

    public SpringDataModule() {
        super(PackageVersion.VERSION);
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Pageable.class, RestCriteria.class);
        this.setAbstractTypes(resolver);
        this.setMixInAnnotation(Page.class, PageMixin.class);
    }

    public static class SpringDataJsonModule extends SpringDataModule {

        public SpringDataJsonModule() {
            super();
            this.addSerializer(new JsonSortSerializer());
            this.addDeserializer(Sort.class, new JsonSortDeserializer());
        }

    }

    public static class SpringDataXmlModule extends SpringDataModule {

        public SpringDataXmlModule() {
            super();
            this.addSerializer(new XmlSortSerializer());
            this.addDeserializer(Sort.class, new XmlSortDeserializer());
        }

    }

    static class JsonSortSerializer extends StdSerializer<Sort> {

        JsonSortSerializer() {
            super(Sort.class);
        }

        @Override
        public void serialize(Sort value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartArray();
            for (Sort.Order order : value) {
                gen.writeStartObject();
                gen.writeStringField(PROPERTY, order.getProperty());
                gen.writeStringField(DIRECTION, order.getDirection().name().toLowerCase());
                gen.writeEndObject();
            }
            gen.writeEndArray();
        }
    }

    static class JsonSortDeserializer extends JsonDeserializer<Sort> {

        @Override
        public Sort deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            ArrayNode node = jp.getCodec().readTree(jp);
            Sort.Order[] orders = new Sort.Order[node.size()];
            int i = 0;
            for (JsonNode obj : node) {
                Sort.Direction direction = obj.get(DIRECTION) != null ?
                        Sort.Direction.valueOf(obj.get(DIRECTION).asText().toUpperCase()) : null;
                orders[i] = new Sort.Order(direction, obj.get(PROPERTY).asText());
                i++;
            }
            return Sort.by(orders);
        }
    }

    static class XmlSortSerializer extends StdSerializer<Sort> {

        protected XmlSortSerializer() {
            super(Sort.class);
        }

        @Override
        public void serialize(Sort value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            SortWrapper sortWrapper = new SortWrapper();
            sortWrapper.setOrders(new ArrayList<>());
            for (Sort.Order order : value) {
                SortWrapper.OrderWrapper orderWrapper = new SortWrapper.OrderWrapper();
                orderWrapper.setProperty(order.getProperty());
                orderWrapper.setDirection(order.getDirection().name().toLowerCase());
                sortWrapper.getOrders().add(orderWrapper);
            }
            gen.writeObject(sortWrapper);
        }

    }

    static class XmlSortDeserializer extends JsonDeserializer<Sort> {

        @Override
        public Sort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            SortWrapper sortWrapper = p.readValueAs(SortWrapper.class);
            return Sort.by(sortWrapper.getOrders().stream().map(orderWrapper -> new Sort.Order(Sort.Direction.valueOf(orderWrapper.getDirection().toUpperCase()), orderWrapper.getProperty())).collect(Collectors.toList()));
        }

    }


    @JsonDeserialize(as = RestPage.class)
    @JsonIgnoreProperties({"last", "number", "numberOfElements", "size", "totalPages", "first", "pageable", "empty"})
    static class PageMixin {
    }

    @JacksonXmlRootElement(localName = "sort")
    private static class SortWrapper {

        private List<OrderWrapper> orders;

        public List<OrderWrapper> getOrders() {
            return orders;
        }

        public void setOrders(List<OrderWrapper> orders) {
            this.orders = orders;
        }

        private static class OrderWrapper {

            private String property;
            private String direction;

            public String getProperty() {
                return property;
            }

            public void setProperty(String property) {
                this.property = property;
            }

            public String getDirection() {
                return direction;
            }

            public void setDirection(String direction) {
                this.direction = direction;
            }

        }

    }

}
