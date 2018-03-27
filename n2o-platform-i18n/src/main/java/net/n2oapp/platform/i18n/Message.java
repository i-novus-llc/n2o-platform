package net.n2oapp.platform.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Локализованное сообщение
 */
public class Message {

    /**
     * Код сообщения
     */
    private String code;
    /**
     * Параметры сообщения
     */
    private Object[] args;

    /**
     * Создать сообщение
     * @param code Код сообщения
     */
    public Message(String code) {
        this.code = code;
    }

    /**
     * Создать сообщение
     * @param code Код сообщения
     * @param args Параметры
     */
    public Message(String code, Object... args) {
        this.code = code;
        this.args = args;
    }

    /**
     * Добавить параметр сообщения
     * @param argument Параметр
     */
    public Message set(Object argument) {
        ArrayList<Object> list = new ArrayList<>(args != null ? Arrays.asList(args) : Collections.emptyList());
        list.add(argument);
        this.args = list.toArray();
        return this;
    }

    /**
     * Получить локализованное сообщение
     * @param messageSourceAccessor Доступ к хранилищу сообщений
     * @return Локализованное сообщение
     */
    public String getMessage(MessageSourceAccessor messageSourceAccessor) {
        return messageSourceAccessor.getMessage(code, args);
    }

    /**
     * Получить локализованное сообщение
     * @param messageSource Хранилище сообщений
     * @return Локализованное сообщение
     */
    public String getMessage(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource).getMessage(code, args);
    }

    /**
     * Получить код сообщения
     * @return Код сообщения
     */
    public String getCode() {
        return code;
    }

    /**
     * Получить параметры сообщения
     * @return Параметры сообщения
     */
    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return code + '{' + Arrays.toString(args) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return Objects.equals(code, message.code) &&
                Arrays.equals(args, message.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }
}
