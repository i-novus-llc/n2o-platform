package net.n2oapp.platform.actuate.autoconfigure;

import net.n2oapp.platform.actuate.health.KafkaHealthIndicator;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.util.TestSocketUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    void kafkaIsUpBySend() {
        KafkaTemplate<String, Object> kafkaTemplate = initKafkaTemplate(kafkaEmbedded.getBrokersAsString());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        setParameters(healthIndicator);

        Health health = healthIndicator.health();

        assert health.getStatus() == Status.UP;
    }

    @Test
    void kafkaIsUpWithAdmin() {
        KafkaTemplate<String, Object> kafkaTemplate = initKafkaTemplateWithAdmin(kafkaEmbedded.getBrokersAsString());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        setParameters(healthIndicator);
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
        KafkaTemplate kafkaTemplate = initKafkaTemplate("127.0.0.1:" + TestSocketUtils.findAvailableTcpPort());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.DOWN;
    }

    @Test
    void kafkaIsDownWithAdmin() {
        KafkaTemplate kafkaTemplate = initKafkaTemplateWithAdmin("127.0.0.1:" + TestSocketUtils.findAvailableTcpPort());

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        setParameters(healthIndicator);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.DOWN;
    }

    @Test
    void kafkaIsDownWithTransactionalTemplateInterruptedException() throws ExecutionException, InterruptedException {
        kafkaIsDownWithTransactionalTemplate(new InterruptedException());
    }

    @Test
    void kafkaIsDownWithTransactionalTemplateExecutionException() throws ExecutionException, InterruptedException {
        kafkaIsDownWithTransactionalTemplate(new ExecutionException("test", new RuntimeException()));
    }

    private void kafkaIsDownWithTransactionalTemplate(Throwable throwable) throws ExecutionException, InterruptedException {
        KafkaTemplate kafkaTemplate = initTxKafkaTemplate(kafkaEmbedded.getBrokersAsString());
        kafkaTemplate = mockSendException(kafkaTemplate, throwable);

        KafkaHealthIndicator healthIndicator = new KafkaHealthIndicator(kafkaTemplate);
        Health health = healthIndicator.health();

        assert health.getStatus() == Status.DOWN;
    }

    private KafkaTemplate mockSendException(KafkaTemplate kafkaTemplate, Throwable throwable) throws ExecutionException, InterruptedException {
        kafkaTemplate = Mockito.spy(kafkaTemplate);
        CompletableFuture completableFuture = Mockito.mock(CompletableFuture.class);
        Mockito.doReturn(completableFuture)
                .when(kafkaTemplate)
                .send(Mockito.any(), Mockito.any());
        Mockito.doThrow(throwable).when(completableFuture).get();
        return kafkaTemplate;
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

    private KafkaTemplate<String, Object> initKafkaTemplateWithAdmin(String bootstrapServers) {
        var kafkaTemplate = initKafkaTemplate(bootstrapServers);
        KafkaAdmin kafkaAdmin = new KafkaAdmin(kafkaTemplate.getProducerFactory().getConfigurationProperties());
        kafkaTemplate.setKafkaAdmin(kafkaAdmin);
        return kafkaTemplate;
    }

    private Map<String, Object> getDefaultKafkaTemplateProps(String bootstrapServers, int timeout) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, timeout); /// timeout reduced for testing speed up
        return config;
    }

    private void setParameters(KafkaHealthIndicator healthIndicator) {
        ReflectionTestUtils.setField(healthIndicator, "maxTimeToWaitMs", 1000L);
    }

}
