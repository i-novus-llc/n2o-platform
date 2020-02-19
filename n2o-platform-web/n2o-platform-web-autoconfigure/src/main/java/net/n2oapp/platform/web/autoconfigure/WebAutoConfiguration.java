package net.n2oapp.platform.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.framework.engine.data.rest.SpringRestDataProviderEngine;
import net.n2oapp.framework.engine.data.rest.json.RestEngineTimeModule;
import net.n2oapp.platform.i18n.Messages;
import org.apache.cxf.jaxrs.client.spring.JaxRsProxyClientConfiguration;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import java.text.SimpleDateFormat;

@Configuration
public class WebAutoConfiguration {

    @Bean
    @Primary
    public PlatformExceptionHandler platformOperationExceptionHandler(@Autowired(required = false) Messages messages) {
        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
        if (messages != null)
            platformExceptionHandler.setMessages(messages);
        return platformExceptionHandler;
    }

    @ConditionalOnBean({OAuth2ProtectedResourceDetails.class, OAuth2ClientContext.class})
    public static class RestConfiguration {

        @Value("${n2o.engine.rest.dateformat.serialize}")
        private String serializingFormat;

        @Value("${n2o.engine.rest.dateformat.deserialize}")
        private String[] deserializingFormats;

        @Bean("oauth2RestTemplate")
        public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails details, OAuth2ClientContext oauth2ClientContext) {
            OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(details, oauth2ClientContext);
            restTemplate.setRetryBadAccessTokens(false);
            return restTemplate;
        }

        @Bean
        @Primary
        public SpringRestDataProviderEngine springRestDataProviderEngine(@Qualifier("oauth2RestTemplate") OAuth2RestTemplate oauth2RestTemplate,
                                                                   @Value("${n2o.engine.rest.url}") String baseRestUrl) {
            ObjectMapper restObjectMapper = new ObjectMapper();
            restObjectMapper.setDateFormat(new SimpleDateFormat(serializingFormat));
            RestEngineTimeModule module = new RestEngineTimeModule(deserializingFormats);
            restObjectMapper.registerModules(module);
            SpringRestDataProviderEngine springRestDataProviderEngine = new SpringRestDataProviderEngine(oauth2RestTemplate, restObjectMapper);
            springRestDataProviderEngine.setBaseRestUrl(baseRestUrl);
            return springRestDataProviderEngine;
        }
    }

    @ConditionalOnBean({OAuth2ProtectedResourceDetails.class, OAuth2ClientContext.class})
    @ConditionalOnClass({AbstractPhaseInterceptor.class, JaxRsProxyClientConfiguration.class})
    public static class ProxyClientConfiguration {
        @Bean
        public JaxRsJwtHeaderInterceptor headerInterceptor() {
            return new JaxRsJwtHeaderInterceptor();
        }
    }
}
