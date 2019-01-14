package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(Client.class)
@ConditionalOnProperty(prefix = "cxf.jaxrs.client", name = "classes-scan", havingValue = "true")
@Import(JaxRsProxyClientRegistrar.class)
@AutoConfigureBefore(CxfAutoConfiguration.class)
public class JaxRsClientAutoConfiguration {
    @Bean
    RestClientExceptionMapper restClientExceptionMapper() {
        return new RestClientExceptionMapper();
    }
}
