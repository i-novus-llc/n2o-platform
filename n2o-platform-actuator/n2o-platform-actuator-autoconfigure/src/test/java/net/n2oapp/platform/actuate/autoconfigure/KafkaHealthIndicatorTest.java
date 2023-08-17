package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.util.SocketUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author RMakhmutov
 * @since 06.09.2018
 */
@SpringBootTest(classes = KafkaHealthIndicatorTest.class)
@EmbeddedKafka(
        // We're only needing to test Kafka serializing interactions, so keep partitioning simple
        partitions = 1,
        brokerProperties = {
                "transaction.state.log.replication.factor=1",
                "transaction.state.log.min.isr=1"
        },
        topics = {
                KafkaHealthIndicatorTest.TEST_TOPIC
        })
class KafkaHealthIndicatorTest {
    public static final String TEST_TOPIC = "test";

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbedded;

    @Test
    void kafkaIsUp() {
        KafkaTemplate<String, Object> kafkaTemplate = initKafkaTemplate(kafkaEmbedded.getBrokersAsString());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.UP;
    }

    @Test
    void kafkaIsUpWithTransactionalTemplate() {
        KafkaTemplate<String, Object> kafkaTemplate = initTxKafkaTemplate(kafkaEmbedded.getBrokersAsString());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.UP;
    }

    @Test
    void kafkaIsDown() {
        KafkaTemplate kafkaTemplate = initKafkaTemplate("127.0.0.1:" + SocketUtils.findAvailableTcpPort());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.DOWN;
    }

    @Test
    void kafkaIsDownWithTransactionalTemplate() {
        KafkaTemplate kafkaTemplate = initTxKafkaTemplate("127.0.0.1:" + SocketUtils.findAvailableTcpPort());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.DOWN;
    }

    private KafkaTemplate<String, Object> initTxKafkaTemplate(String bootstrapServers) {
        Map<String, Object> config = getTransactionalKafkaTemplateProps(bootstrapServers, 3000);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
    }

    private KafkaTemplate<String, Object> initKafkaTemplate(String bootstrapServers) {
        Map<String, Object> config = getDefaultKafkaTemplateProps(bootstrapServers, 1000);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
    }

    private Map<String, Object> getTransactionalKafkaTemplateProps(String bootstrapServers, int timeout) {
        Map<String, Object> config = getDefaultKafkaTemplateProps(bootstrapServers, timeout);
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-" + UUID.randomUUID());
        return config;
    }

    private Map<String, Object> getDefaultKafkaTemplateProps(String bootstrapServers, int timeout) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, timeout); /// timeout reduced for testing speed up
        return config;
    }

}
