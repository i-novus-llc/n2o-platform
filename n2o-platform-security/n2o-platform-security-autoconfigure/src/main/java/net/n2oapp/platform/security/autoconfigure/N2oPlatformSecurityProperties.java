package net.n2oapp.platform.security.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "n2o.platform.security")
public class N2oPlatformSecurityProperties {
    /**
     * Адрес сервиса, возвращающего сертификаты SSO сервера
     */
    private String keySetUri;
    /**
     * Проверять ли действительность токена
     */
    private boolean checkTokenExpired = true;

    /**
     * Проверять ли в токене поле aud
     */
    private boolean checkAud = true;

    /**
     * Идентификатор ресурса (должен соответствовать значению поля aud в токене, часто совпадает с clientId)
     */
    private String resourceId;

    /**
     * Идентификатор клиента
     */
    private String clientId;
    /**
     * Секретный код клиента
     */
    private String clientSecret;
    /**
     * Адрес сервера аутентификации
     */
    private String accessTokenUri;

    /**
     * Ключ имени пользователя
     */
    private String usernameKey = "username";
    /**
     * Ключ прав доступа
     */
    private String authoritiesKey = "roles";
    /**
     * Префикс прав доступа
     */
    private String authoritiesPrefix = "";
    /**
     * Права доступа в верхнем регистре
     */
    private boolean authoritiesUpperCase = false;
    /**
     * Права доступа в нижнем регистре
     */
    private boolean authoritiesLowerCase = false;

    public String getKeySetUri() {
        return keySetUri;
    }

    public void setKeySetUri(String keySetUri) {
        this.keySetUri = keySetUri;
    }

    public boolean isCheckTokenExpired() {
        return checkTokenExpired;
    }

    public void setCheckTokenExpired(boolean checkTokenExpired) {
        this.checkTokenExpired = checkTokenExpired;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        if (resourceId == null)
            this.resourceId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessTokenUri() {
        return accessTokenUri;
    }

    public void setAccessTokenUri(String accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    public boolean isCheckAud() {
        return checkAud;
    }

    public void setCheckAud(boolean checkAud) {
        this.checkAud = checkAud;
    }

    public String getUsernameKey() {
        return usernameKey;
    }

    public void setUsernameKey(String usernameKey) {
        this.usernameKey = usernameKey;
    }

    public String getAuthoritiesKey() {
        return authoritiesKey;
    }

    public void setAuthoritiesKey(String authoritiesKey) {
        this.authoritiesKey = authoritiesKey;
    }

    public String getAuthoritiesPrefix() {
        return authoritiesPrefix;
    }

    public void setAuthoritiesPrefix(String authoritiesPrefix) {
        this.authoritiesPrefix = authoritiesPrefix;
    }

    public boolean isAuthoritiesUpperCase() {
        return authoritiesUpperCase;
    }

    public void setAuthoritiesUpperCase(boolean authoritiesUpperCase) {
        this.authoritiesUpperCase = authoritiesUpperCase;
    }

    public boolean isAuthoritiesLowerCase() {
        return authoritiesLowerCase;
    }

    public void setAuthoritiesLowerCase(boolean authoritiesLowerCase) {
        this.authoritiesLowerCase = authoritiesLowerCase;
    }
}
