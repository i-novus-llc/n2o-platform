package net.n2oapp.platform.jaxrs;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Преобразование исключений JSR303 в сообщения {@link RestMessage} REST сервиса
 */
@Provider
public class ViolationRestExceptionMapper implements RestExceptionMapper<ValidationException> {

    @Override
    public RestMessage toMessage(ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            Set<ConstraintViolation<?>> constraintViolations = ((ConstraintViolationException)exception).getConstraintViolations();
            List<RestMessage.Error> errors = constraintViolations.stream()
                    .map(c -> new RestMessage.Error(c.getPropertyPath().toString(), c.getMessage()))
                    .collect(Collectors.toList());
            return new RestMessage(errors);
        } else {
            return new RestMessage(exception.getMessage());
        }
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
