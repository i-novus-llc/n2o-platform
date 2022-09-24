package net.n2oapp.platform.loader.client;

import net.n2oapp.platform.loader.client.auth.AuthRestTemplate;
import net.n2oapp.platform.loader.client.auth.BasicAuthClientContext;
import net.n2oapp.platform.loader.client.auth.ClientContext;
import net.n2oapp.platform.loader.client.auth.OAuth2ClientContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthRestTemplateTest {

    @LocalServerPort
    private String port;

    @Test
    public void testBasicAuthContext() {
        ClientContext clientContext = new BasicAuthClientContext("user", "password");
        assertThat(clientContext.getTokenType(), is("Basic"));
        assertThat(clientContext.getAccessToken(), is("dXNlcjpwYXNzd29yZA=="));
    }

    @Test
    public void testOauth2Context() {
        ClientContext clientContext = new OAuth2ClientContext("testClient", "testClientSecret", "http://localhost:" + port + "/token");
        assertThat(clientContext.getTokenType(), is("Bearer"));
        assertThat(clientContext.getAccessToken(), is("test_token"));
    }

    @Test
    public void testAuthRestTemplate() {
        //token acquire failed
        try {
            RestOperations template = new AuthRestTemplate(Map.of("http://localhost:" + port + "/resource", new OAuth2ClientContext("testClient", "testClientSecret", "http://localhost:" + port + "/hello")));
            template.getForEntity("http://localhost:" + port + "/resource", String.class);
            assert false;
        } catch (Exception e) {
            assertThat(e instanceof IllegalStateException, is(true));
        }

        //oauth success
        RestOperations template = new AuthRestTemplate(Map.of("http://localhost:" + port + "/resource", new OAuth2ClientContext("testClient", "testClientSecret", "http://localhost:" + port + "/token")));
        ResponseEntity<String> token = template.getForEntity("http://localhost:" + port + "/resource", String.class);
        assertThat(token.getBody(), is("Bearer test_token"));

        //basic auth success
        template = new AuthRestTemplate(Map.of("http://localhost:" + port + "/resource", new BasicAuthClientContext("user", "password")));
        token = template.getForEntity("http://localhost:" + port + "/resource", String.class);
        assertThat(token.getBody(), is("Basic dXNlcjpwYXNzd29yZA=="));
    }
}
