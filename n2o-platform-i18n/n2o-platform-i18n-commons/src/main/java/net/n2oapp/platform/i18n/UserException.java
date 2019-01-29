package net.n2oapp.platform.i18n;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Исключение, содержащее локализованное сообщение, понятное конечному пользователю.
 */
public class UserException extends RuntimeException {
    private static final long serialVersionUID = 2552353701499979545L;
    private transient Object[] args;
    private final transient List<Message> messages;

    public UserException(Message message) {
        super(message.getCode());
        this.args = message.getArgs();
        this.messages = null;
    }

    public UserException(List<Message> messages) {
        super(messages.stream().map(Message::getCode).collect(Collectors.joining(",\n")));
        this.messages = Collections.unmodifiableList(messages);
    }


    public UserException(Message message, Throwable cause) {
        super(message.getCode(), cause);
        this.args = message.getArgs();
        this.messages = null;
    }

    public UserException(String code) {
        super(code);
        this.messages = null;
    }

    public UserException(String code, Throwable cause) {
        super(code, cause);
        this.messages = null;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public String getCode() {
        return getMessage();
    }

    public List<Message> getMessages() {
        return messages;
    }

}
