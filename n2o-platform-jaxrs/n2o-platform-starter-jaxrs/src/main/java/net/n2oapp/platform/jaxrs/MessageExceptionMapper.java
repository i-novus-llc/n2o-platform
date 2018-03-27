package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.i18n.MessageException;
import net.n2oapp.platform.i18n.Messages;

import javax.ws.rs.ext.Provider;

/**
 * Конвертация исключения {@link MessageException}, содержащего локализованное сообщение, в ответ REST сервиса.
 */
@Provider
public class MessageExceptionMapper implements RestExceptionMapper<MessageException> {

    private Messages messages;

    public MessageExceptionMapper(Messages messages) {
        this.messages = messages;
    }

    @Override
    public RestMessage toMessage(MessageException exception) {
        return new RestMessage(messages.getMessage(exception.getMessage(), exception.getArgs()));
    }
}
