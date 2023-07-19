package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;

/**
 * Load default properties with low priority.
 * These properties are applied like any other autoconfiguration properties,
 * after all other spring configuration is done but before context initialization.
 *
 * @author RMakhmutov
 * @since 11.01.2019
 */
@AutoConfiguration
@PropertySource("classpath:cloud.n2o.default.properties")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CloudDefaultPropertiesAutoConfiguration {
}
