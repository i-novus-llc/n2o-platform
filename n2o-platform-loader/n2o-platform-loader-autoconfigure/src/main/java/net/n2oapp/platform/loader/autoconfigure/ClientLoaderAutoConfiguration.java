package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@ConditionalOnClass(ClientLoader.class)
@EnableConfigurationProperties(ClientLoaderProperties.class)
public class ClientLoaderAutoConfiguration {

    @Autowired
    private ClientLoaderProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public JsonClientLoader jsonClientLoader() {
        return new JsonClientLoader(new RestTemplate());
    }

    @Bean
    @ConditionalOnMissingBean
    public LoaderRunner loaderRunner(List<ClientLoader> loaders,
                                     @Autowired(required = false) List<ClientLoaderConfigurer> configurers) {
        LoaderRunner runner = new LoaderRunner(loaders);
        runner.setFailFast(properties.isFailFast());
        properties.getCommands().forEach(runner::add);
        if (configurers != null)
            configurers.forEach(c -> c.configure(runner));
        return runner;
    }

    @Bean
    @Conditional(RunAfterStartedCondition.class)
    @ConditionalOnMissingBean
    public LoaderStarter startAfterUp(LoaderRunner runner) {
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
    public LoaderStarter startOnDeploy(LoaderRunner runner) {
        return new LoaderStarter(runner) {
            @Override
            @PostConstruct
            public void start() {
                LoaderReport report = runner.run();
                if (properties.isFailFast() && !report.isSuccess())
                    throw new IllegalStateException("Loader failed", report.getFails().get(0).getException());
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
}
