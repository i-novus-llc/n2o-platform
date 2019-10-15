package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Запускатель загрузчиков json данных
 */
public class JsonLoaderRunner extends BaseLoaderRunner implements ServerLoaderRestService {
    private ObjectMapper objectMapper;

    public JsonLoaderRunner(List<ServerLoader> loaders, ObjectMapper objectMapper) {
        super(loaders);
        this.objectMapper = objectMapper;
    }

    protected List<Object> read(InputStream body, LoaderDataInfo<?> info) {
        List<Object> data;
        try {
            CollectionType type = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, info.getDataType());
            data = objectMapper.readValue(body, type);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Cannot read body for %s", info.getTarget()), e);
        }
        return data;
    }
}
