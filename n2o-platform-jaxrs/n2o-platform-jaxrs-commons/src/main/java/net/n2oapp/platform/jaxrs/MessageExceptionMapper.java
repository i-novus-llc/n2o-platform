package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.i18n.Message;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.i18n.Messages;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

/**
 * Конвертация исключения {@link UserException}, содержащего локализованное сообщение, в ответ REST сервиса.
 */
@Provider
public class MessageExceptionMapper implements RestExceptionMapper<UserException> {

    private Messages messages;

    public MessageExceptionMapper(Messages messages) {
        this.messages = messages;
    }

    @Override
    public RestMessage toMessage(UserException exception) {
        if (exception.getMessages() != null) {
            return new RestMessage(exception.getMessages().stream().map(this::toError).collect(Collectors.toList()));
        }
        return new RestMessage(messages.getMessage(exception.getMessage(), exception.getArgs()));
    }

    private RestMessage.Error toError(Message message) {
        return new RestMessage.Error(messages.getMessage(message.getCode(), message.getArgs()));
    }

    @Override
    public Response.Status getStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
