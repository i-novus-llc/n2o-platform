package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * This configuration defines default properties.
 * These properties are applied before all spring environment postprocessors and spring context initialization.
 * Used only for "bootstrap" properties, that should be defined before spring configuration begins.
 */
public class CloudBootstrapEnvironmentPostprocessor implements EnvironmentPostProcessor, Ordered {
    public static final String CONSUL_PROPERTIES_NAME = "n2oConsulProperties";
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        if (!propertySources.contains(CONSUL_PROPERTIES_NAME))
            propertySources.addLast(new PropertiesPropertySource(CONSUL_PROPERTIES_NAME, defaultConsulProperties()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
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
