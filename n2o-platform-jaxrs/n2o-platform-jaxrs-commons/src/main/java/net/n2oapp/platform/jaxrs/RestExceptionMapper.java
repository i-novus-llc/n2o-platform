package net.n2oapp.platform.jaxrs;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Преобразование исключений в ответы REST сервиса
 */
public interface RestExceptionMapper<E extends Throwable> extends ExceptionMapper<E> {

    @Override
    default Response toResponse(E exception) {
        return Response.status(getStatus())
                .entity(toMessage(exception))
                .header("exception-class", RestException.class.getName())
                .type("application/json;charset=UTF-8")
                .build();
    }

    RestMessage toMessage(E exception);

    default Response.Status getStatus() {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }
}
