package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.Map;

/**
 * @author RMakhmutov
 * @since 24.08.2018
 */
@Configuration
@ConditionalOnWebApplication
@PropertySource("classpath:/META-INF/net/n2oapp/platform/actuate/monitoring.properties")
@AutoConfigureBefore(ServletManagementContextAutoConfiguration.class)
public class ActuatorAutoConfiguration {
    static final String ACTUATOR_CONTEXT_PATH = "/monitoring";

    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class MonitoringSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher(ACTUATOR_CONTEXT_PATH + "/**").authorizeRequests().anyRequest().permitAll();
        }
    }

    @Configuration
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnEnabledHealthIndicator("kafka")
    public static class KafkaHealthIndicatorConfiguration extends
            CompositeHealthIndicatorConfiguration<KafkaHealthIndicator, KafkaTemplate> {

        private final Map<String, KafkaTemplate> kafkaTemplates;

        public KafkaHealthIndicatorConfiguration(
                Map<String, KafkaTemplate> kafkaTemplates) {
            this.kafkaTemplates = kafkaTemplates;
        }

        @Bean
        @ConditionalOnMissingBean(name = "kafkaHealthIndicator")
        public HealthIndicator kafkaHealthIndicator() {
            return createHealthIndicator(this.kafkaTemplates);
        }

    }
}
