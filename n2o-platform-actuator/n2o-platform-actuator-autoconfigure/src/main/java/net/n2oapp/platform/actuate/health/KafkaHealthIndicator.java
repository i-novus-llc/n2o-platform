package net.n2oapp.platform.actuate.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class KafkaHealthIndicator extends AbstractHealthIndicator {

    @Value("${management.health.kafka.max-time-to-wait-ms:5000}")
    private Long maxTimeToWaitMs;

    private static final String HEALTHCHECK_TOPIC = "test";

    private final KafkaTemplate kafkaTemplate;
    private final AdminClient adminClient;

    public KafkaHealthIndicator(KafkaTemplate kafkaTemplate) {
        Assert.notNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.kafkaTemplate = kafkaTemplate;
        KafkaAdmin kafkaAdmin = kafkaTemplate.getKafkaAdmin();
        if (kafkaAdmin != null) {
            this.adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        } else {
            this.adminClient = null;
        }
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (adminClient != null) {
            doHealthCheckByAdmin(builder);
        } else {
            doHealthCheckBySend(builder);
        }
    }

    private void doHealthCheckByAdmin(Health.Builder builder) {
        try {
            adminClient.listTopics().names().get(maxTimeToWaitMs, MILLISECONDS);
            builder.up();
        } catch (InterruptedException ex) {
            builder.down(ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            builder.down(ex);
        }
    }

    private void doHealthCheckBySend(Health.Builder builder) {
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
