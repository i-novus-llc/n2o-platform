package net.n2oapp.platform.ms.autoconfigure.app;

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
 * @since 14.01.2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes= CloudBootstrapAutoConfigurationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class CloudBootstrapAutoConfigurationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    Environment env;

    @Test
    public void testStartupApplication() {
        ActuatorHealthResponse response = restTemplate.getForObject(env.getProperty("management.endpoints.web.base-path") + "/health", ActuatorHealthResponse.class);
        assert response.getStatus().equals(Status.UP.getCode()) : "Application startup failed";
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