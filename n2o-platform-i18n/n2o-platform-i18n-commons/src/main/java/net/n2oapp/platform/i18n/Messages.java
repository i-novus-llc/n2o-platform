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
        String defaultMessage = code.contains("{0}") ? MessageFormat.format(code, args) : code;
        return messageSource.getMessage(code, args, defaultMessage);
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
     * Если исключение {@link UserException}, то локализованное сообщение с параметрами.
     * @param exception Исключение
     * @return Локализованное сообщение
     */
    public String getMessage(Exception exception) {
        if (exception instanceof UserException)
            return getMessage(exception.getMessage(), ((UserException)exception).getArgs());
        else
            return getMessage(exception.getMessage());
    }
}
