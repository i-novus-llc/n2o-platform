package net.n2oapp.platform.ms.autoconfigure.log;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

public class InLineLoggingConfiguratorListener implements ApplicationListener, Ordered {

    private int positionBeforeLoggingApplicationListener = 19;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ConfigurableEnvironment environment = ((ApplicationEnvironmentPreparedEvent) event).getEnvironment();
            String inlineLogging = environment.getProperty("logging.inline");
            if (Boolean.FALSE.equals(Boolean.valueOf(inlineLogging)))
                return;

            Properties props = new Properties();
            props.put("logging.config", "classpath:logback-inline.xml");
            environment.getPropertySources().addFirst(new PropertiesPropertySource("loggingProps", props));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + positionBeforeLoggingApplicationListener;
    }
}
