package net.n2oapp.platform.ms.autoconfigure.logging;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * Add a 'logging.config' property with path to inline xml config file,
 * so next in chain {@link LoggingApplicationListener} will load our custom config.
 * <p>
 * {@link OneLinePatternLayout} and {@link OneLineStacktraceConverter} will be pulled up
 * by {@link LoggingApplicationListener logging listener} through this xml config.
 */
public class OneLineLoggingConfiguratorListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    /**
     * Need to launch {@link OneLineLoggingConfiguratorListener this} before {@link LoggingApplicationListener}.
     */
    private static final int POSITION_BEFORE_LOGGING_APPLICATION_LISTENER = LoggingApplicationListener.DEFAULT_ORDER - 1;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        String inlineLogging = environment.getProperty("n2o-boot-platform.logging.oneline.enabled");
        if (Boolean.FALSE.equals(Boolean.valueOf(inlineLogging)))
            return;

        Properties props = new Properties();
        props.put("logging.config", "classpath:logback-oneline.xml");
        environment.getPropertySources().addFirst(new PropertiesPropertySource("loggingProps", props));
    }

    @Override
    public int getOrder() {
        return POSITION_BEFORE_LOGGING_APPLICATION_LISTENER;
    }
}
