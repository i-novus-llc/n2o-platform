package net.n2oapp.platform.i18n;

/**
 * Исключение, содержащее локализованное сообщение
 */
public class MessageException extends RuntimeException {
    private static final Message UNEXPECTED_ERROR = new Message("exception.unexpectedError");

    private Message message;

    public MessageException(Message message) {
        super(message.getCode());
        this.message = message;
    }

    public MessageException(Message message, Throwable cause) {
        super(message.getCode(), cause);
    }

    public MessageException(String code) {
        this(new Message(code));
    }

    public MessageException(String code, Throwable cause) {
        this(new Message(code), cause);
    }

    public MessageException() {
        this(UNEXPECTED_ERROR);
    }

    public MessageException(Throwable cause) {
        this(UNEXPECTED_ERROR, cause);
    }

    public Object[] getArgs() {
        return this.message.getArgs();
    }

    /**
     * Добавить параметр сообщения
     * @param argument Параметр
     */
    public MessageException set(Object argument) {
        this.message.set(argument);
        return this;
    }
}
