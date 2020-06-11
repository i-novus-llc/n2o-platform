package net.n2oapp.platform.actuate.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author RMakhmutov
 * @since 06.09.2018
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={ActuatorAutoConfigurationTest.class,TestWebSecurityConfig.class,TestKafkaConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class ActuatorAutoConfigurationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Test
    public void testActuatorAutoConfiguration() {
        ActuatorHealthResponse response = restTemplate.getForObject(env.getProperty("management.endpoints.web.base-path") + "/health", ActuatorHealthResponse.class);

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
