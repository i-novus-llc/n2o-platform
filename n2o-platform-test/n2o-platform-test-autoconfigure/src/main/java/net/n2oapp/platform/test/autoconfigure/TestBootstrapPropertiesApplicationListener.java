package net.n2oapp.platform.test.autoconfigure;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * This configuration defines default properties for test cloud environment with low priority.
 * These properties are applied before all spring environment postprocessors and spring context initialization.
 * Use only for "bootstrap" properties, that should be defined before spring configuration begins.
 */
public class TestBootstrapPropertiesApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    public static final String CONSUL_PROPERTIES_NAME = "testDefaultConsulProperties";

    @Override
    public int getOrder() {
        return EnvironmentPostProcessorApplicationListener.DEFAULT_ORDER - 2; /// before environment postprocessors and CloudDefaultPropertiesApplicationListener
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        MutablePropertySources propertySources = event.getEnvironment().getPropertySources();
        if (!propertySources.contains(CONSUL_PROPERTIES_NAME))
            propertySources.addLast(new PropertiesPropertySource(CONSUL_PROPERTIES_NAME, testDefaultConsulProperties()));
    }

    private Properties testDefaultConsulProperties() {
        Properties props = new Properties();
        /// Disable consul configuration import
        props.put("spring.config.import", "");
        props.put("spring.cloud.consul.enabled", "false");
        return props;
    }
}
