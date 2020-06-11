package net.n2oapp.platform.actuate.autoconfigure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;

/**
 * @author keddok
 * @since 29.04.2020
 */
@TestConfiguration
public class TestKafkaConfig {
    @Bean
    public KafkaTemplate initKafkaTemplate() {
        return new KafkaTemplate(new DefaultKafkaProducerFactory(new HashMap<>()));
    }
}
