package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * This configuration defines default properties for cloud environment with low priority.
 * These properties are applied before all spring environment preprocessors and spring context initialization.
 * Use only for "bootstrap" properties, that should be defined before spring configuration begins.
 */
public class CloudBootstrapPropertiesApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    public static final String CONSUL_PROPERTIES_NAME = "defaultConsulProperties";

    @Override
    public int getOrder() {
        return EnvironmentPostProcessorApplicationListener.DEFAULT_ORDER - 1; /// before all spring environment preprocessors (contain checks for the presence of specific properties)
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        MutablePropertySources propertySources = event.getEnvironment().getPropertySources();
        if (!propertySources.contains(CONSUL_PROPERTIES_NAME))
            propertySources.addLast(new PropertiesPropertySource(CONSUL_PROPERTIES_NAME, defaultConsulProperties()));
    }

    private Properties defaultConsulProperties() {
        Properties props = new Properties();
        props.put("spring.config.import", "consul:");
        props.put("spring.cloud.consul.config.enabled", "true");
        props.put("spring.cloud.consul.host", "consul-agent.local");
        props.put("spring.cloud.consul.port", "8500");
        props.put("spring.cloud.consul.config.format", "YAML");
        return props;
    }
}
