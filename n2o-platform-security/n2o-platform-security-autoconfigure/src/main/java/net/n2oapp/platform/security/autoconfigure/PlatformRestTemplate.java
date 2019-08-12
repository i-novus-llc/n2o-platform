package net.n2oapp.platform.security.autoconfigure;

import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * Шаблон вызова Rest запросов с передачей токена доступа и опциональной проверкой срока действия токена
 */
public class PlatformRestTemplate extends OAuth2RestTemplate {
    /**
     * Прверять срок действия токена?
     */
    private boolean checkTokenExpired = true;

    public PlatformRestTemplate(OAuth2ProtectedResourceDetails resource) {
        super(resource);
    }

    public PlatformRestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
        super(resource, context);
    }

    public void setCheckTokenExpired(boolean checkTokenExpired) {
        this.checkTokenExpired = checkTokenExpired;
    }

    /**
     * Получение или обновление токена, опциональная проверка срока действия, настраивается через {@link #setCheckTokenExpired(boolean)}
     * @return Токен доступа
     */
    @Override
    public OAuth2AccessToken getAccessToken() throws UserRedirectRequiredException {
        OAuth2AccessToken accessToken = getOAuth2ClientContext().getAccessToken();
        if (accessToken != null && !checkTokenExpired)
            return accessToken;
        return super.getAccessToken();
    }
}
