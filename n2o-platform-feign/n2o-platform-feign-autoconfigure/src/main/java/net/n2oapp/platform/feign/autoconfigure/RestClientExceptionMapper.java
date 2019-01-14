package net.n2oapp.platform.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Преобразование ответа REST сервиса в исключение {@link RestException}
 */
public class RestClientExceptionMapper implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public RestClientExceptionMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        Collection<String> headers = response.headers().get("exception-class");
        if (headers != null) {
            Iterator<String> iterator = headers.iterator();
            if (iterator.hasNext() && RestException.class.getName().equalsIgnoreCase(iterator.next())) {
                try {
                    RestMessage message = objectMapper.readValue(response.body().asInputStream(), RestMessage.class);
                    return new RestException(message);
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot decode message body", e);
                }
            }
        }
        return null;
    }
}
