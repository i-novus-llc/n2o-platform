package net.n2oapp.platform.actuate.autoconfigure;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author keddok
 * @since 29.04.2020
 */
@Configuration
public class TestKafkaConfig {
    @Bean
    public KafkaTemplate initKafkaTemplate() {
        return new KafkaTemplate(new DefaultKafkaProducerFactory(new HashMap<>()));
    }
}
