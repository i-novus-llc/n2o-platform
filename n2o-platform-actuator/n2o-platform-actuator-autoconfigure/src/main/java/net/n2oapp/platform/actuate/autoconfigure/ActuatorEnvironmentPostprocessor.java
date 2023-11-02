package net.n2oapp.platform.actuate.autoconfigure;

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
public class ActuatorEnvironmentPostprocessor implements EnvironmentPostProcessor, Ordered {
    public static final String ACTUATOR_PROPERTIES_NAME = "n2oSpringActuatorProperties";
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        if (!propertySources.contains(ACTUATOR_PROPERTIES_NAME))
            propertySources.addLast(new PropertiesPropertySource(ACTUATOR_PROPERTIES_NAME, defaultActuatorProperties()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private Properties defaultActuatorProperties() {
        Properties props = new Properties();
        props.put("management.endpoints.web.exposure.include", "*");
        props.put("management.endpoint.health.show-details", "always");
        return props;
    }
}
