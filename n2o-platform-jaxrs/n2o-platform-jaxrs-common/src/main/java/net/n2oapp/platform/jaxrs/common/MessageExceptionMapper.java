package net.n2oapp.platform.jaxrs.common;

import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.i18n.Messages;

import javax.ws.rs.ext.Provider;

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
        return new RestMessage(messages.getMessage(exception.getMessage(), exception.getArgs()));
    }
}
