package net.n2oapp.platform.security.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "n2o.platform.security")
public class N2oPlatformSecurityProperties {
    /**
     * URL адрес сервиса, возвращающего сертификаты SSO сервера
     */
    private String keySetUri;
    /**
     * Проверять ли действительность токена
     */
    private boolean checkTokenExpired = true;

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
}
