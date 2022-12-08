package net.n2oapp.platform.quartz.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:quartz.default.properties")
public class QuartzAutoConfiguration {

}
