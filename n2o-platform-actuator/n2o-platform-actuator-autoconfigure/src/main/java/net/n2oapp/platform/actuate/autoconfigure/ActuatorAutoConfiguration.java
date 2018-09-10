package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

/**
 * @author RMakhmutov
 * @since 24.08.2018
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureBefore(ManagementServerPropertiesAutoConfiguration.class)
public class ActuatorAutoConfiguration {
    static final String ACTUATOR_CONTEXT_PATH = "/monitoring";

    private ManagementServerProperties managementServerProperties;

    @Bean
    @ConditionalOnMissingBean
    public ManagementServerProperties managementServerProperties() {
        if (managementServerProperties == null) {
            ManagementServerProperties properties = new ManagementServerProperties();
            properties.setContextPath(ACTUATOR_CONTEXT_PATH);
            properties.getSecurity().setEnabled(false);
            managementServerProperties = properties;
        }

        return managementServerProperties;
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
