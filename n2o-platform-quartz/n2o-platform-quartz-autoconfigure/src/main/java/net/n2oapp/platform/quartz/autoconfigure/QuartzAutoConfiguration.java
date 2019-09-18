package net.n2oapp.platform.quartz.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:quartz.default.properties")
public class QuartzAutoConfiguration {

}
