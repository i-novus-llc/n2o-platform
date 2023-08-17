package net.n2oapp.platform.actuate.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class KafkaHealthIndicator extends AbstractHealthIndicator {

    private static final String HEALTHCHECK_TOPIC = "test";

    private final KafkaTemplate kafkaTemplate;

    public KafkaHealthIndicator(KafkaTemplate kafkaTemplate) {
        Assert.notNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            if (kafkaTemplate.isTransactional()) {
                kafkaTemplate.executeInTransaction(kafkaOperations -> send());
            } else {
                send();
            }
            builder.up();
        } catch (Exception ex) {
            builder.down(ex);
        }
    }

    private Object send() {
        try {
            return kafkaTemplate.send(HEALTHCHECK_TOPIC, null).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException | CancellationException e) {
            throw new IllegalStateException(e);
        }
    }

}
