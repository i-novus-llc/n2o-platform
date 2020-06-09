package net.n2oapp.platform.actuate.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

public class KafkaHealthIndicator extends AbstractHealthIndicator {

    private final KafkaTemplate kafkaTemplate;

    public KafkaHealthIndicator(KafkaTemplate kafkaTemplate) {
        Assert.notNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            kafkaTemplate.send("test", null).get();
            builder.up();
        } catch (Exception ex) {
            builder.down(ex);
        }
    }
}
