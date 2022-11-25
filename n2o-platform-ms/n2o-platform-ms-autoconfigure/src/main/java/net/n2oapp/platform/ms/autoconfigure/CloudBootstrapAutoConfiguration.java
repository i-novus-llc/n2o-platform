package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;

/**
 * @author RMakhmutov
 * @since 11.01.2019
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(name = {"ConsulConfigAutoConfiguration"})
@PropertySource("classpath:cloud.n2o.default.bootstrap.properties")
public class CloudBootstrapAutoConfiguration {
}
