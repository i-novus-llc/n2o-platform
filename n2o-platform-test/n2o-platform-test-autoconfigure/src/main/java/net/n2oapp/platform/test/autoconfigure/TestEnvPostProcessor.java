package net.n2oapp.platform.test.autoconfigure;

import net.n2oapp.platform.test.PortFinder;
import net.n2oapp.platform.test.autoconfigure.pg.EnableEmbeddedPg;
import net.n2oapp.platform.test.autoconfigure.pg.EnableTestcontainersPg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.HashMap;
import java.util.Map;

public class TestEnvPostProcessor implements EnvironmentPostProcessor {
    private static final String PROPERTY_SOURCE_NAME = TestPropertySourceUtils.INLINED_PROPERTIES_PROPERTY_SOURCE_NAME;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        Class clazz = springApplication.getMainApplicationClass();
        Map<String, Object> map = new HashMap<>();
        if (clazz.getAnnotation(DefinePort.class) != null) {
            map.put("server.port", PortFinder.getPort("spring-boot"));
        }
        if(clazz.getAnnotation(EnableEmbeddedPg.class) != null) {
            map.put("test.embedded-pg", true);
        }
        if(clazz.getAnnotation(EnableTestcontainersPg.class) != null) {
            map.put("test.testcontainers-pg", true);
        }

        if(!map.isEmpty()) {
            addOrReplace(configurableEnvironment.getPropertySources(), map);
        }

    }

    private void addOrReplace(MutablePropertySources propertySources, Map<String, Object> map) {
        MapPropertySource target = null;
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
            if (source instanceof MapPropertySource) {
                target = (MapPropertySource) source;
                for (Map.Entry<String,Object> entry : map.entrySet()) {
                    target.getSource().put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (target == null) {
            target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
        }
        if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.addLast(target);
        }
    }
}

