package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Запускатель загрузчиков json данных
 */
public class JsonLoaderRunner extends BaseLoaderRunner {

    private ObjectMapper objectMapper;

    public JsonLoaderRunner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected Object read(InputStream body, ServerLoaderCommand command) {
        Object data;
        try {
            if (command.isIterable()) {
                CollectionType type = objectMapper.getTypeFactory()
                        .constructCollectionType(command.getIterableType(), command.getElementType());
                data = objectMapper.readValue(body, type);
            } else {
                data = objectMapper.readValue(body, command.getType());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return data;
    }
}
