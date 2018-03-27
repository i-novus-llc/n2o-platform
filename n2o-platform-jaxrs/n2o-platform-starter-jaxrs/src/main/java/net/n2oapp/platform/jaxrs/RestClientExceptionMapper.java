package net.n2oapp.platform.jaxrs;

import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Преобразование ответа REST сервиса в исключение {@link RestException}
 */
@Provider
public class RestClientExceptionMapper implements ResponseExceptionMapper {
    @Override
    public Throwable fromResponse(Response response) {
        if (RestException.class.getName().equals(response.getHeaderString("exception-class"))) {
            RestMessage message = response.readEntity(RestMessage.class);
            return new RestException(message);
        } else {
            return null;
        }
    }
}
