package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

/**
 * @author RMakhmutov
 * @since 24.08.2018
 */
@AutoConfiguration
@ConditionalOnWebApplication
@AutoConfigureBefore(ManagementContextAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class ActuatorAutoConfiguration {

    @Configuration
    @ConditionalOnClass(value = {SecurityFilterChain.class, WebSecurityCustomizer.class})
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
            super(KafkaHealthIndicator::new);
            this.kafkaTemplates = kafkaTemplates;
        }

        @Bean
        @ConditionalOnMissingBean(name = "kafkaHealthContributor")
        public HealthContributor kafkaHealthContributor() {
            return createContributor(this.kafkaTemplates);
        }
    }
}
