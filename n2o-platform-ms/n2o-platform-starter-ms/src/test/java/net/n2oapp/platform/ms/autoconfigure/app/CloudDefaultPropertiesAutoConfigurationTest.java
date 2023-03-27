package net.n2oapp.platform.ms.autoconfigure.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;

/**
 * @author RMakhmutov
 * @since 14.01.2019
 */
@SpringBootTest(classes = CloudDefaultPropertiesAutoConfigurationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "n2o.ms.loki.enabled=true" /// test that enabled loki doesn't fails app context load
        })
@EnableAutoConfiguration
class CloudDefaultPropertiesAutoConfigurationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    Environment env;

    @Test
    void testStartupApplication() {
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