package net.n2oapp.platform.jaxrs.autoconfigure;

import brave.Tracing;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.apache.cxf.tracing.brave.jaxrs.BraveClientProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(Client.class)
@AutoConfigureBefore(CxfAutoConfiguration.class)
public class JaxRsClientAutoConfiguration {
    @Bean
    RestClientExceptionMapper restClientExceptionMapper() {
        return new RestClientExceptionMapper();
    }

    @Bean
    @ConditionalOnProperty(value = {"spring.sleuth.enabled"})
    BraveClientProvider braveClientProvider(Tracing brave) {
        return new BraveClientProvider(brave);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "cxf.jaxrs.client", name = "classes-scan", havingValue = "true")
    @Import(JaxRsProxyClientRegistrar.class)
    static class JaxRsProxyClientScanAutoConfiguration {}
}
