package net.n2oapp.platform.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootApplication(exclude = OAuth2AutoConfiguration.class)
public class WebTest extends WebSecurityConfigurerAdapter {

    @LocalServerPort
    private int port;

    @Test
    public void pageUp() {
        RestTemplate restTemplate = new RestTemplate();
        Map<?, ?> indexPage = restTemplate.getForObject("http://localhost:" + port + "/n2o/page/", Map.class);
        assertThat(indexPage, notNullValue());
    }
}
