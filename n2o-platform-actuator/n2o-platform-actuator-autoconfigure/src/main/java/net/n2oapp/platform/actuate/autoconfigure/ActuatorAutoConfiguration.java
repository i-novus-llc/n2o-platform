package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

/**
 * @author RMakhmutov
 * @since 24.08.2018
 */
@AutoConfiguration
@ConditionalOnWebApplication
@PropertySource("classpath:/META-INF/net/n2oapp/platform/actuate/monitoring.properties")
@AutoConfigureBefore(ServletManagementContextAutoConfiguration.class)
public class ActuatorAutoConfiguration {

    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnClass(SecurityFilterChain.class)
    @ConditionalOnBean(HttpSecurity.class)
    @AutoConfigureAfter(SecurityAutoConfiguration.class)
    public static class ActuatorSecurityCustomizer {
        @Bean
        public WebSecurityCustomizer actuatorSecurityCustomizer() {
            return web -> web.ignoring().requestMatchers(EndpointRequest.toAnyEndpoint());
        }
    }

    @Configuration
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnEnabledHealthIndicator("kafka")
    public static class KafkaHealthIndicatorConfiguration extends
            CompositeHealthContributorConfiguration<KafkaHealthIndicator, KafkaTemplate> {

        private final Map<String, KafkaTemplate> kafkaTemplates;

        public KafkaHealthIndicatorConfiguration(Map<String, KafkaTemplate> kafkaTemplates) {
            this.kafkaTemplates = kafkaTemplates;
        }

        @Bean
        @ConditionalOnMissingBean(name = "kafkaHealthContributor")
        public HealthContributor kafkaHealthContributor() {
            return createContributor(this.kafkaTemplates);
        }
    }
}
