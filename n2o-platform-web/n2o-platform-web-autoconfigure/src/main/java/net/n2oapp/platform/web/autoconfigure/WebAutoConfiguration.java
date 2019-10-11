package net.n2oapp.platform.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.framework.boot.N2oAutoConfiguration;
import net.n2oapp.framework.engine.data.rest.SpringRestDataProviderEngine;
import net.n2oapp.framework.engine.data.rest.json.RestEngineTimeModule;
import net.n2oapp.platform.i18n.Messages;
import org.apache.cxf.jaxrs.client.spring.JaxRsProxyClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import java.text.SimpleDateFormat;

@Configuration
@AutoConfigureBefore(N2oAutoConfiguration.class)
public class WebAutoConfiguration {

    @Bean
    public PlatformExceptionHandler platformOperationExceptionHandler(@Autowired(required = false) Messages messages) {
        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
        if (messages != null)
            platformExceptionHandler.setMessages(messages);
        return platformExceptionHandler;
    }

    @ConditionalOnBean({OAuth2ProtectedResourceDetails.class, OAuth2ClientContext.class})
    public static class RestConfiguration {

        @Bean("oauth2RestTemplate")
        public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails details, OAuth2ClientContext oauth2ClientContext) {
            OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(details, oauth2ClientContext);
            restTemplate.setRetryBadAccessTokens(false);
            return restTemplate;
        }

        @Bean("restDataProviderEngine")
        @ConditionalOnMissingBean(name = "restDataProviderEngine")
        public SpringRestDataProviderEngine restDataProviderEngine(@Qualifier("oauth2RestTemplate") OAuth2RestTemplate oauth2RestTemplate,
                                                                   @Value("${n2o.engine.rest.url}") String baseRestUrl) {
            ObjectMapper restObjectMapper = new ObjectMapper();
            restObjectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
            RestEngineTimeModule module = new RestEngineTimeModule(new String[]{"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"});
            restObjectMapper.registerModules(module);
            SpringRestDataProviderEngine springRestDataProviderEngine = new SpringRestDataProviderEngine(oauth2RestTemplate, restObjectMapper);
            springRestDataProviderEngine.setBaseRestUrl(baseRestUrl);
            return springRestDataProviderEngine;
        }

        @Bean
        @ConditionalOnClass(JaxRsProxyClientConfiguration.class)
        public JaxRsJwtHeaderInterceptor headerInterceptor() {
            return new JaxRsJwtHeaderInterceptor();
        }

    }
}
