package net.n2oapp.platform.ms.autoconfigure.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * @author RMakhmutov
 * @since 14.01.2019
 *
 * Check that application can start successfully with certain properties when Consul is unavailable.
 * Test for compatibility with older platform versions.
 */
@SpringBootTest(classes = ConsulUnavailableTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.consul.host=invalidaddress", /// emulate unavailability
                "spring.cloud.consul.config.fail-fast=false", /// do not fail app start if consul unavailable
                "management.health.consul.enabled=false"}) /// do not fail healthcheck if consul unavailable
@EnableAutoConfiguration
public class ConsulUnavailableTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    Environment env;

    @Test
    public void testStartupApplicationWithoutConsul() {
        ActuatorHealthResponse response = restTemplate.getForObject(env.getProperty("management.endpoints.web.base-path") + "/health", ActuatorHealthResponse.class);
        assert response.getStatus().equals(Status.UP.getCode()) : "Application startup failed";
    }

    private static class ActuatorHealthResponse {
        private String status;

        public String getStatus() {
            return status;
        }
    }
}