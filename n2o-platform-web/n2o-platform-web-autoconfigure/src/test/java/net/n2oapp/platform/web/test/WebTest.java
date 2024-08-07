package net.n2oapp.platform.web.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = WebTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
class WebTest {

    @LocalServerPort
    private int port;

    @Value("${server.forward-headers-strategy}")
    private String forwardHeadersStrategy;

    @Test
    void propertiesLoaded() {
        assertThat(forwardHeadersStrategy, is("native"));
    }

    @Test
    void pageUp() {
        RestTemplate restTemplate = new RestTemplate();
        Map<?, ?> indexPage = restTemplate.getForObject("http://localhost:" + port + "/n2o/page/", Map.class);
        assertThat(indexPage, notNullValue());
    }
}
