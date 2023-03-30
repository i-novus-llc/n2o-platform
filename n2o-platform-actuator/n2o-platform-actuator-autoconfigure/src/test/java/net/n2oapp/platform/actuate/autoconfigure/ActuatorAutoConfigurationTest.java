package net.n2oapp.platform.actuate.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author RMakhmutov
 * @since 06.09.2018
 */
@SpringBootTest(classes={ActuatorAutoConfigurationTest.class,TestKafkaConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class ActuatorAutoConfigurationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    WebEndpointProperties webEndpointProperties;

    @Test
    void testActuatorAutoConfiguration() {
        ActuatorHealthResponse response = restTemplate.getForObject(webEndpointProperties.getBasePath() + "/health", ActuatorHealthResponse.class);

        /// assert that health endpoint is up and accessible, and that kafka is down and overall application health is "DOWN"
        assert response.getStatus().equals(Status.DOWN.getCode());
    }

    private static class ActuatorHealthResponse {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
