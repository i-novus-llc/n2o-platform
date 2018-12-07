package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.*;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.util.SocketUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author RMakhmutov
 * @since 06.09.2018
 */
public class KafkaHealthIndicatorTest {
    @ClassRule
    public static EmbeddedKafkaRule kafkaEmbedded = new EmbeddedKafkaRule(1);

    private KafkaTemplate kafkaTemplate;

    @Test
    public void kafkaIsUp() throws Exception {
        initKafkaTemplate(this.kafkaEmbedded.getEmbeddedKafka().getBrokersAsString());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(this.kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.UP;
    }

    @Test
    public void kafkaIsDown() {
        initKafkaTemplate("127.0.0.1:" + SocketUtils.findAvailableTcpPort());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(this.kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.DOWN;
    }

    private void initKafkaTemplate(String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000); /// timeout reduced for testing speed up
        this.kafkaTemplate = new KafkaTemplate(new DefaultKafkaProducerFactory(config));
    }
}
