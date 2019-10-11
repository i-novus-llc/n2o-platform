package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.io.InputStream;

public class JsonLoaderEngine implements LoaderEndpoint {

    private LoaderRegister register;
    private ObjectMapper objectMapper;

    public JsonLoaderEngine(LoaderRegister register, ObjectMapper objectMapper) {
        this.register = register;
        this.objectMapper = objectMapper;
    }

    @Override
    public void load(String subject, String target, InputStream body) {
        LoaderInfo loader = find(target);
        Object data = read(body, loader);
        execute(subject, loader, data);
    }

    protected LoaderInfo find(String target) {
        return register.find(target);
    }

    @SuppressWarnings("unchecked")
    protected void execute(String subject, LoaderInfo info, Object data) {
        ServerLoader<?> loader = info.getLoader();
        ((ServerLoader<Object>)loader).load(data, subject);
    }

    protected Object read(InputStream body, LoaderInfo info) {
        Object data;
        try {
            if (info.isIterable()) {
                CollectionType type = objectMapper.getTypeFactory()
                        .constructCollectionType(info.getIterableType(), info.getElementType());
                data = objectMapper.readValue(body, type);
            } else {
                data = objectMapper.readValue(body, info.getType());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return data;
    }
}
