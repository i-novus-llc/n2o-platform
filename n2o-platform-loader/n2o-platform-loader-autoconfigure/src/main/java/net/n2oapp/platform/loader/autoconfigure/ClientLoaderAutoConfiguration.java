package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.*;
import net.n2oapp.platform.loader.client.ClientLoaderCommand.AuthDetails;
import net.n2oapp.platform.loader.client.auth.AuthRestTemplate;
import net.n2oapp.platform.loader.client.auth.BasicAuthClientContext;
import net.n2oapp.platform.loader.client.auth.ClientContext;
import net.n2oapp.platform.loader.client.auth.OAuth2ClientContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnClass(ClientLoader.class)
@EnableConfigurationProperties(ClientLoaderProperties.class)
public class ClientLoaderAutoConfiguration {

    private static final String ENDPOINT_PATTERN = "/loaders/{subject}/{target}";

    @Bean
    @ConditionalOnMissingBean(name = "clientLoaderRestTemplate")
    public RestOperations clientLoaderRestTemplate(@Autowired(required = false) List<RestTemplateCustomizer> customizers,
                                                   @Autowired(required = false) Map<String, ClientContext> contextMap) {
        RestTemplate restTemplate = new AuthRestTemplate(contextMap);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.setMessageConverters(converters);
        if (customizers != null)
            customizers.forEach(c -> c.customize(restTemplate));
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonClientLoader jsonClientLoader(@Qualifier("clientLoaderRestTemplate") RestOperations clientLoaderRestTemplate) {
        JsonClientLoader loader = new JsonClientLoader(clientLoaderRestTemplate);
        loader.setEndpointPattern(ENDPOINT_PATTERN);
        return loader;
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientLoaderRunner loaderRunner(List<ClientLoader> loaders,
                                           @Autowired(required = false) List<ClientLoaderConfigurer> configurers,
                                           ClientLoaderProperties properties) {
        ClientLoaderRunner runner = new ClientLoaderRunner(loaders);
        runner.setFailFast(properties.isFailFast());
        properties.getCommands().forEach(runner::add);
        if (configurers != null)
            configurers.forEach(c -> c.configure(runner));
        return runner;
    }

    @Bean
    @ConditionalOnMissingBean(name = "contextStorage")
    public Map<String, ClientContext> contextStorage(ClientLoaderProperties properties) {
        Map<String, ClientContext> contextMap = new HashMap<>();
        properties.getCommands().forEach(c -> {
            if (c.getAuth() != null) {
                String url = c.getServerUri();
                url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
                url = url + ENDPOINT_PATTERN.replace("{subject}", c.getSubject()).replace("{target}", c.getTarget());
                AuthDetails auth = c.getAuth();
                if ("oauth2".equals(auth.getType())) {
                    ClientContext ctx = new OAuth2ClientContext(
                            auth.getClientId(),
                            auth.getClientSecret(),
                            auth.getTokenUri());
                    contextMap.put(url, ctx);
                } else if ("basic".equals(auth.getType())) {
                    ClientContext ctx = new BasicAuthClientContext(
                            auth.getUsername(),
                            auth.getPassword());
                    contextMap.put(url, ctx);
                }
            }
        });

        return contextMap;
    }

    @Bean
    @ConditionalOnProperty(prefix = "n2o.loader.client", name="start", havingValue = "UP", matchIfMissing = true)
    @ConditionalOnMissingBean
    public LoaderStarter startAfterUp(ClientLoaderRunner runner) {
        return new LoaderStarter(runner) {
            @Override
            @EventListener(ApplicationReadyEvent.class)
            public void start() {
                super.start();
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "n2o.loader.client", name="start", havingValue = "DEPLOY")
    @ConditionalOnMissingBean
    public LoaderStarter startOnDeploy(ClientLoaderRunner runner, ClientLoaderProperties properties) {
        return new LoaderStarter(runner) {
            @Override
            @PostConstruct
            public void start() {
                LoaderReport report = runner.run();
                if (properties.isFailFast() && !report.isSuccess())
                    throw new IllegalStateException(report.getFails().get(0).getException());
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "n2o.loader.client", name="start", havingValue = "MANUAL")
    @ConditionalOnMissingBean
    public LoaderStarter startManual(ClientLoaderRunner runner) {
        return new LoaderStarter(runner);
    }

    @Configuration
    @ConditionalOnClass(HealthIndicator.class)
    @AutoConfigureAfter(ClientLoaderAutoConfiguration.class)
    static class ClientLoaderActuatorConfiguration {
        @Bean
        ClientLoaderStarterEndpoint clientLoaderStarterEndpoint() {
            return new ClientLoaderStarterEndpoint();
        }

        @Bean
        ClientLoaderHealthIndicator clientLoaderHealthIndicator(LoaderStarter starter, ClientLoaderProperties properties) {
            return new ClientLoaderHealthIndicator(starter, properties);
        }
    }
}
