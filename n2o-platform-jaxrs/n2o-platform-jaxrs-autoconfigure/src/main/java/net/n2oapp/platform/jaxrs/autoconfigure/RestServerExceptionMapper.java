package net.n2oapp.platform.jaxrs.autoconfigure;

import net.n2oapp.platform.jaxrs.CodeModel;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestExceptionMapper;
import net.n2oapp.platform.jaxrs.RestMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Преобразование любых исключений в ответ {@link RestMessage} REST сервиса
 */
@Provider
public class RestServerExceptionMapper implements RestExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(RestServerExceptionMapper.class);
    private boolean canExportStack;

    public RestMessage toMessage(Exception exception) {
        CodeModel code = CodeModel.buildCode(exception.getMessage());
        logger.error(code.getCode(), exception);
        RestMessage message = new RestMessage(code.getCode());
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

    public void setCanExportStack(boolean canExportStack) {
        this.canExportStack = canExportStack;
    }
}
