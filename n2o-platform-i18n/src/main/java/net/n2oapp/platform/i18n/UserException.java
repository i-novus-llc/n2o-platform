package net.n2oapp.platform.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Исключение, содержащее локализованное сообщение, понятное конечному пользователю.
 */
public class UserException extends RuntimeException {
    private static final Message UNEXPECTED_ERROR = new Message("exception.unexpectedError");

    private Object[] args;

    public UserException(Message message) {
        super(message.getCode());
        this.args = message.getArgs();
    }

    public UserException(Message message, Throwable cause) {
        super(message.getCode(), cause);
        this.args = message.getArgs();
    }

    public UserException(String code) {
        super(code);
    }

    public UserException(String code, Throwable cause) {
        super(code, cause);
    }

    public UserException() {
        this(UNEXPECTED_ERROR);
    }

    public UserException(Throwable cause) {
        this(UNEXPECTED_ERROR, cause);
    }

    public Object[] getArgs() {
        return this.args;
    }

    public String getCode() {
        return getMessage();
    }

    /**
     * Добавить параметр сообщения
     * @param argument Параметр
     */
    public UserException set(Object argument) {
        ArrayList<Object> list = new ArrayList<>(args != null ? Arrays.asList(args) : Collections.emptyList());
        list.add(argument);
        this.args = list.toArray();
        return this;
    }
}
