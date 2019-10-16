package net.n2oapp.platform.loader.client;

/**
 * Исключение при загрузке данных
 */
public class LoadingException extends RuntimeException {

    public LoadingException(String message) {
        super(message);
    }

    public LoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
