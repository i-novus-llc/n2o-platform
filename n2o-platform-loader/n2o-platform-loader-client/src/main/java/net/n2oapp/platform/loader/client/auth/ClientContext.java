package net.n2oapp.platform.loader.client.auth;

/**
 * Контекст авторизации клиента
 */
public interface ClientContext {
    /**
     * Получить токен доступа
     *
     * @return токен доступа
     */
    String getAccessToken();

    /**
     * Получить тип токена доступа
     *
     * @return тип токена доступа
     */
    String getTokenType();
}
