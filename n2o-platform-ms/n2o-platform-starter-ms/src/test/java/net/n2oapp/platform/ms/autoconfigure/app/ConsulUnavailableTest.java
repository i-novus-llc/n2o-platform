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
 * Check that application can start successfully with certain properties when Consul Config is unavailable.
 * Test for compatibility with older platform versions.
 * @author RMakhmutov
 * @since 14.01.2019
 */
@SpringBootTest(classes = ConsulUnavailableTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.consul.enabled=true", /// enable consul auto configuration (disabled in platform test starter)
                "spring.config.import=optional:consul:", /// set consul optional
                "spring.cloud.consul.config.enabled=true", /// enable consul config
                "spring.cloud.consul.host=invalidaddress", /// emulate unavailability
                "management.health.consul.enabled=false"}) /// do not fail healthcheck if consul unavailable
@EnableAutoConfiguration
class ConsulUnavailableTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    Environment env;

    @Test
    void testStartupApplicationWithoutConsul() {
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