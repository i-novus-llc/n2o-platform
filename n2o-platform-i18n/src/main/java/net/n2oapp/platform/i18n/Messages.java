package net.n2oapp.platform.i18n;

import org.springframework.context.support.MessageSourceAccessor;

import java.text.MessageFormat;

/**
 * Класс для удобной локализации сообщений с параметрами.
 * При отсутствии кода сообщения в хранилище, возвращается сам код.
 */
public class Messages {
    private MessageSourceAccessor messageSource;

    /**
     * Конструктор
     * @param messageSource Доступ к хранилищу сообщений
     */
    public Messages(MessageSourceAccessor messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Получить локализованное сообщение
     * @param code Код сообщения
     * @param args Параметры сообщения
     * @return Локализованное сообщение
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, MessageFormat.format(code, args));
    }

    /**
     * Получить локализованное сообщение
     * @param message Сообщение
     * @return Локализованное сообщение
     */
    public String getMessage(Message message) {
        return getMessage(message.getCode(), message.getArgs());
    }

    /**
     * Получить локализованное сообщение из произвольного исключения.
     * Если исключение {@link MessageException}, то локализованное сообщение с параметрами.
     * @param exception Исключение
     * @return Локализованное сообщение
     */
    public String getMessage(Exception exception) {
        if (exception instanceof MessageException)
            return getMessage(exception.getMessage(), ((MessageException)exception).getArgs());
        else
            return getMessage(exception.getMessage());
    }
}
