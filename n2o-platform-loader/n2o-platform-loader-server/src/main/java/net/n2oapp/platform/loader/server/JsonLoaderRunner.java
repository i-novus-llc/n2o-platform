package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Запускатель загрузчиков json данных
 */
public class JsonLoaderRunner extends BaseLoaderRunner {
    private ObjectMapper objectMapper;

    public JsonLoaderRunner(List<ServerLoader> loaders, ObjectMapper objectMapper) {
        super(loaders);
        this.objectMapper = objectMapper;
    }

    protected Object read(InputStream body, ServerLoaderRoute route) {
        Object data;
        try {
            if (route.isIterable()) {
                CollectionType type = objectMapper.getTypeFactory()
                        .constructCollectionType(route.getIterableType(), route.getElementType());
                data = objectMapper.readValue(body, type);
            } else {
                data = objectMapper.readValue(body, route.getType());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return data;
    }
}
