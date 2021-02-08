package net.n2oapp.platform.jaxrs.autoconfigure;

import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.jaxrs.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Преобразование любых исключений в ответ {@link RestMessage} REST сервиса
 */
@Provider
public class RestServerExceptionMapper implements RestExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(RestServerExceptionMapper.class);
    private boolean canExportStack;
    @Autowired
    private Messages messages;

    public RestMessage toMessage(Exception exception) {
        CodeGenerator generator = new CodeGenerator(getPrefix());
        String code = generator.generate();
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

    public void setCanExportStack(boolean canExportStack) {
        this.canExportStack = canExportStack;
    }
}
