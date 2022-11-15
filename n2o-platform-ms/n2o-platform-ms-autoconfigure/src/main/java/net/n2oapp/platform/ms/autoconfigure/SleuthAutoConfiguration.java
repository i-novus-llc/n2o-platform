package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author RMakhmutov
 * @since 08.04.2019
 */
@AutoConfiguration
@PropertySource("classpath:sleuth.n2o.default.properties")
public class SleuthAutoConfiguration {
}
