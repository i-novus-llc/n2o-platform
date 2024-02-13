package net.n2oapp.platform.jaxrs.autoconfigure;

import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.jaxrs.CodeGenerator;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestExceptionMapper;
import net.n2oapp.platform.jaxrs.RestMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Преобразование любых исключений в ответ {@link RestMessage} REST сервиса
 */
@Provider
public class RestServerExceptionMapper implements RestExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(RestServerExceptionMapper.class);
    private boolean canExportStack;
    private Messages messages;

    public RestServerExceptionMapper(boolean canExportStack, Messages messages) {
        this.canExportStack = canExportStack;
        this.messages = messages;
    }

    public RestMessage toMessage(Exception exception) {
        String code = CodeGenerator.generate(getPrefix());
        logger.error(code, exception);
        RestMessage message = new RestMessage(code);
        if (canExportStack) message.setStackTrace(ExceptionUtils.getStackFrames(exception));
        return message;
    }

    @Override
    public Response toResponse(Exception exception) {
        return Response.status(this.getStatus())
                .entity(this.toMessage(exception))
                .header("exception-class", RestException.class.getName())
                .type("application/json;charset=UTF-8")
                .build();
    }


    private String getPrefix() {
        String prefix = messages.getMessage("ui.error-prefix");
        if (prefix.equals("ui.error-prefix"))
            return null;
        else return prefix;
    }
}
