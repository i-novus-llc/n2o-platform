package net.n2oapp.platform.actuate.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author RMakhmutov
 * @since 06.09.2018
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=ActuatorAutoConfigurationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class ActuatorAutoConfigurationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testActuator() {
        ActuatorHealthResponse response = restTemplate.getForObject(ActuatorAutoConfiguration.ACTUATOR_CONTEXT_PATH + "/health", ActuatorHealthResponse.class);
        assert response.getStatus().equals(Status.UP.getCode());
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
