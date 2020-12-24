package net.n2oapp.platform.ms.autoconfigure.log;

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
 * {@link InLinePatternLayout} and {@link InLineStacktraceConverter} will be pulled up
 * by {@link LoggingApplicationListener logging listener} through this xml config.
 */
public class InLineLoggingConfiguratorListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    /**
     * Need to launch {@link InLineLoggingConfiguratorListener this} before {@link LoggingApplicationListener}.
     */
    private static final int POSITION_BEFORE_LOGGING_APPLICATION_LISTENER = LoggingApplicationListener.DEFAULT_ORDER - 1;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        String inlineLogging = environment.getProperty("n2o-boot-platform.logging.inline.enabled");
        if (Boolean.FALSE.equals(Boolean.valueOf(inlineLogging)))
            return;

        Properties props = new Properties();
        props.put("logging.config", "classpath:logback-inline.xml");
        environment.getPropertySources().addFirst(new PropertiesPropertySource("loggingProps", props));
    }

    @Override
    public int getOrder() {
        return POSITION_BEFORE_LOGGING_APPLICATION_LISTENER;
    }
}
