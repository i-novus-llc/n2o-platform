package net.n2oapp.platform.ms.autoconfigure.logging;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * {@code LogbackApplicationListener} is a programmatic logging system configuration, faster alternative to a standard logback xml file configuration.
 * <p>
 * It adds a Loki appender over the existing logging configuration. Loki appender is only added if {@code n2o.ms.loki.enabled} setting is enabled.
 * Loki appender is added to the ROOT logger.
 * <p>
 * It also replaces layout and encoder all appenders specified in the settings({@code n2o.ms.logging.json.appender_names}) for printing logs in Json format.
 * This only applies by setting {@code n2o.ms.logging.json.enabled}.
 * <p>
 * {@code LogbackApplicationListener} is spring factory class that works during start of the application in two phases:
 * <ol>
 * <li> After preparing the application environment by the {@code ApplicationEnvironmentPreparedEvent}.
 * Used for compatibility with standard spring boot logging system configuration.</li>
 * <li> After initializing(or reinitializing) the Spring context.
 * This phase is used for compatibility with the spring cloud bootstrap configuration.</li>
 * </ol>
 *
 * @author RMakhmutov
 * @since 09.09.2021
 */
@Configuration(proxyBeanMethods = false)
public class LogbackApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>,
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 20;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        configureLogging(event.getEnvironment());
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        configureLogging(applicationContext.getEnvironment());
    }

    private void configureLogging(ConfigurableEnvironment env) {
        LoggerConfigurator loggerConfigurator = new LoggerConfigurator(env);
        loggerConfigurator.configureJsonFormatIfRequired();
        loggerConfigurator.configureLokiAppenderIfRequired();
    }

}