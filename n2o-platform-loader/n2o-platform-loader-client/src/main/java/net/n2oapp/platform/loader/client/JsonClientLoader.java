package net.n2oapp.platform.loader.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonClientLoader extends RestClientLoader {
    private ObjectMapper objectMapper;

    public JsonClientLoader(RestOperations restTemplate, ObjectMapper objectMapper) {
        super((restTemplate));
        this.objectMapper = objectMapper;
    }

    @Override
    protected Object getData(Resource file) {
        List<Map> data;
        try {
            data = objectMapper.readValue(file.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return data;
    }
}
