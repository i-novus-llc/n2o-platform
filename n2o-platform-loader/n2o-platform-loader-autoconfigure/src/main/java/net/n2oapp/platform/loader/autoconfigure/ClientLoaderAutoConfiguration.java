package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestOperations;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

@Configuration
@ConditionalOnClass(ClientLoader.class)
@EnableConfigurationProperties(ClientLoaderProperties.class)
public class ClientLoaderAutoConfiguration {

    @Autowired
    private ClientLoaderProperties properties;

    @Bean
    @ConditionalOnMissingBean(name = "clientLoaderRestTemplate")
    public RestOperations clientLoaderRestTemplate(@Autowired(required = false) List<RestTemplateCustomizer> customizers) {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .additionalMessageConverters(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        if (customizers != null)
            builder.additionalCustomizers(customizers);
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonClientLoader jsonClientLoader(@Qualifier("clientLoaderRestTemplate") RestOperations clientLoaderRestTemplate) {
        return new JsonClientLoader(clientLoaderRestTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientLoaderRunner loaderRunner(List<ClientLoader> loaders,
                                           @Autowired(required = false) List<ClientLoaderConfigurer> configurers) {
        ClientLoaderRunner runner = new ClientLoaderRunner(loaders);
        runner.setFailFast(properties.isFailFast());
        properties.getCommands().forEach(runner::add);
        if (configurers != null)
            configurers.forEach(c -> c.configure(runner));
        return runner;
    }

    @Bean
    @Conditional(RunAfterStartedCondition.class)
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
    @Conditional(RunOnDeployCondition.class)
    @ConditionalOnMissingBean
    public LoaderStarter startOnDeploy(ClientLoaderRunner runner) {
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

    static class RunAfterStartedCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ClientLoaderProperties.StartingTime runningTime = context.getEnvironment().getProperty("n2o.loader.client.start", ClientLoaderProperties.StartingTime.class);
            if (runningTime == null)
                return true;
            return runningTime.equals(ClientLoaderProperties.StartingTime.UP);
        }
    }

    static class RunOnDeployCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ClientLoaderProperties.StartingTime runningTime = context.getEnvironment().getProperty("n2o.loader.client.start", ClientLoaderProperties.StartingTime.class);
            if (runningTime == null)
                return false;
            return runningTime.equals(ClientLoaderProperties.StartingTime.DEPLOY);
        }
    }

    @Configuration
    @ConditionalOnClass(HealthIndicator.class)
    static class ClientLoaderActuatorConfiguration {
        @Bean
        ClientLoaderStarterEndpoint clientLoaderStarterEndpoint() {
            return new ClientLoaderStarterEndpoint();
        }
        @Bean
        @ConditionalOnBean(LoaderStarter.class)
        ClientLoaderHealthIndicator clientLoaderHealthIndicator(LoaderStarter starter) {
            return new ClientLoaderHealthIndicator(starter);
        }
    }
}
