package net.n2oapp.platform.web.autoconfigure;

import org.apache.cxf.annotations.Provider;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Добавление токена в заголовки реквестов
 */
@Provider(value = Provider.Type.OutInterceptor)
public class HeaderInterceptor extends AbstractPhaseInterceptor<Message> {

    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    public HeaderInterceptor() {
        super(Phase.WRITE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message message) {
        if (oauth2ClientContext != null) {
            String tokenType = oauth2ClientContext.getAccessToken().getTokenType();
            if (!StringUtils.hasText(tokenType)) {
                tokenType = OAuth2AccessToken.BEARER_TYPE;
            }
            Map<String, List> headers = (Map<String, List>) message.get("org.apache.cxf.message.Message.PROTOCOL_HEADERS");
            headers.put("Authorization", Collections.singletonList(String.format("%s %s", tokenType,
                    oauth2ClientContext.getAccessToken().getValue())));
        }
    }
}
