package net.n2oapp.platform.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.framework.boot.N2oFrameworkAutoConfiguration;
import net.n2oapp.framework.engine.data.rest.SpringRestDataProviderEngine;
import net.n2oapp.framework.engine.data.rest.json.RestEngineTimeModule;
import net.n2oapp.platform.i18n.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;

@AutoConfiguration
@AutoConfigureBefore(N2oFrameworkAutoConfiguration.class)
@PropertySource("classpath:web.n2o.default.properties")
public class WebAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public PlatformExceptionHandler platformOperationExceptionHandler(@Autowired(required = false) Messages messages) {
        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
        if (messages != null)
            platformExceptionHandler.setMessages(messages);
        return platformExceptionHandler;
    }

    @Configuration
    @ConditionalOnClass(name = "net.n2oapp.platform.userinfo.config.InterceptorConfig")
    public static class RestConfiguration {

        @Value("${n2o.engine.rest.dateformat.serialize}")
        private String serializingFormat;

        @Value("${n2o.engine.rest.dateformat.deserialize}")
        private String[] deserializingFormats;

        @Value("${n2o.engine.rest.dateformat.exclusion-keys}")
        private String[] exclusionKeys;

        @Bean("restDataProviderEngine")
        @ConditionalOnMissingBean(name = "restDataProviderEngine")
        public SpringRestDataProviderEngine oauthRestDataProviderEngine(@Qualifier("platformRestTemplate") RestTemplate restTemplate,
                                                                        @Value("${n2o.engine.rest.url}") String baseRestUrl) {
            ObjectMapper restObjectMapper = new ObjectMapper();
            restObjectMapper.setDateFormat(new SimpleDateFormat(serializingFormat));
            RestEngineTimeModule module = new RestEngineTimeModule(deserializingFormats, exclusionKeys);
            restObjectMapper.registerModules(module);
            SpringRestDataProviderEngine springRestDataProviderEngine = new SpringRestDataProviderEngine(restTemplate, restObjectMapper);
            springRestDataProviderEngine.setBaseRestUrl(baseRestUrl);
            return springRestDataProviderEngine;
        }
    }
}
