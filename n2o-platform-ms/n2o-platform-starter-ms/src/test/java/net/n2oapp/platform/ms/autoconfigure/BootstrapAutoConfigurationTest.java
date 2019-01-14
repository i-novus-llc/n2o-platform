package net.n2oapp.platform.ms.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author RMakhmutov
 * @since 14.01.2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=BootstrapAutoConfigurationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class BootstrapAutoConfigurationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testStartupApplication() {
        ActuatorHealthResponse response = restTemplate.getForObject("/monitoring/health", ActuatorHealthResponse.class);
        assert response.getStatus().equals("UP") : "Application startup failed";
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