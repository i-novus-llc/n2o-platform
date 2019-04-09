package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author RMakhmutov
 * @since 11.01.2019
 */
@Configuration
@PropertySource("classpath:cloud.n2o.default.bootstrap.properties")
public class CloudBootstrapAutoConfiguration {
}
