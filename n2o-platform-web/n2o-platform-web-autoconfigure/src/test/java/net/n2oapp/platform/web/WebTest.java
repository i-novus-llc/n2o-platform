package net.n2oapp.platform.web;

import net.n2oapp.platform.web.api.TestRestService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=62517",
                "security.oauth2.client.client-id=test",
                "cxf.path=/api",
                "cxf.jaxrs.client.classes-scan=true",
                "cxf.jaxrs.client.classes-scan-packages=net.n2oapp.platform.web.api",
                "cxf.jaxrs.client.address=http://localhost:${server.port}/token"
        })
@SpringBootApplication
public class WebTest {

    private static final String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private static final RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    private int port;

    @MockBean
    private OAuth2ClientContext clientContext;

    @Autowired
    @Qualifier("testRestServiceJaxRsProxyClient")
    private TestRestService testRestService;


    @Before
    public void setUp() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(jwt);
        token.setTokenType("Bearer");
        Mockito.when(clientContext.getAccessToken()).thenReturn(token);
    }

    @Test
    public void testN2o() {
        Map<?, ?> indexPage = restTemplate.getForObject("http://localhost:" + port + "/n2o/page/", Map.class);
        assertThat(indexPage, notNullValue());
    }

    @Test
    public void testRestProvider() {
        restTemplate.getForObject("http://localhost:" + port + "/n2o/page/", Map.class);
        Map result = restTemplate.getForObject("http://localhost:" + port + "/n2o/data/", Map.class);
        assertThat(((Map) ((List) result.get("list")).get(0)).get("auth"), is("Bearer " + jwt));
    }

    @Test
    public void testJaxRsProxyClient() {
        Map result = testRestService.getToken();
        assertThat(((Map) ((List) result.get("token")).get(0)).get("auth"), is("Bearer " + jwt));
    }

    @TestConfiguration
    public static class WebTestConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().permitAll();
        }
    }
}
