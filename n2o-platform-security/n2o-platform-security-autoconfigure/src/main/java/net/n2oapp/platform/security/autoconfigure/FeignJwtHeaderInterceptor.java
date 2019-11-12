package net.n2oapp.platform.security.autoconfigure;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Component
@ConditionalOnClass(Feign.class)
public class FeignJwtHeaderInterceptor implements RequestInterceptor {

    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (oauth2ClientContext != null
                && oauth2ClientContext.getAccessToken() != null) {
            String tokenType = oauth2ClientContext.getAccessToken().getTokenType();
            if (!StringUtils.hasText(tokenType)) {
                tokenType = OAuth2AccessToken.BEARER_TYPE;
            }
            requestTemplate.header("Authorization", Collections.singletonList(String.format("%s %s", tokenType,
                    oauth2ClientContext.getAccessToken().getValue())));
        }
    }
}
