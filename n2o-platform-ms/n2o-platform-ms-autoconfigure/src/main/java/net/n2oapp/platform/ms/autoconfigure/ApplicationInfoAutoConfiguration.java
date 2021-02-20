package net.n2oapp.platform.ms.autoconfigure;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

@Configuration
@AutoConfigureAfter(ProjectInfoAutoConfiguration.class)
public class ApplicationInfoAutoConfiguration {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Order
    @ConditionalOnBean(BuildProperties.class)
    public ApplicationInfoPrinter applicationInfoPrinter(BuildProperties properties) {
        return new ApplicationInfoPrinter(properties);
    }
}
