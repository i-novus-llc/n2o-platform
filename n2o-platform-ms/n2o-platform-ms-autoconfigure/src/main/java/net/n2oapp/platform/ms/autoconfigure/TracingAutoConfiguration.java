package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;

/**
 * @author RMakhmutov
 * @since 08.04.2019
 */
@AutoConfiguration
@PropertySource("classpath:tracing.n2o.default.properties")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class TracingAutoConfiguration {
}
