package net.n2oapp.platform.userinfo.test;

import net.n2oapp.security.auth.common.OauthUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {UserInfoTest.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.security.strategy=GLOBAL")
@EnableAutoConfiguration
public class UserInfoTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void userinfoWebClientTest() {
        SecurityContextHolder.setContext(new SecurityContextImpl(oAuth2AuthenticationToken()));
        ResponseEntity<Boolean> nextServiceContextCorrect = webClient.get().uri("http://localhost:" + port).retrieve().toEntity(Boolean.class).block();
        assertThat(nextServiceContextCorrect.getBody(), is(true));
    }

    @Test
    public void userinfoRestTemplate() {
        SecurityContextHolder.setContext(new SecurityContextImpl(oAuth2AuthenticationToken()));
        ResponseEntity<Boolean> nextServiceContextCorrect = restTemplate.getForEntity("http://localhost:" + port, Boolean.class);
        assertThat(nextServiceContextCorrect.getBody(), is(true));
    }

    @Test
    public void noAuthRestTemplate() {
        ResponseEntity<Boolean> nextServiceContextCorrect = restTemplate.getForEntity("http://localhost:" + port, Boolean.class);
        assertThat(nextServiceContextCorrect.getBody(), is(false));
    }

    private OAuth2AuthenticationToken oAuth2AuthenticationToken() {
        OidcIdToken oidcIdToken = new OidcIdToken("test_token_value", Instant.MIN, Instant.MAX, Map.of("sub", "sub"));
        OauthUser oauthUser = new OauthUser("admin", oidcIdToken);
        oauthUser.setEmail("test@i-novus.ru");
        oauthUser.setSurname("admin");
        OAuth2AuthenticationToken oAuth2AuthenticationToken = new OAuth2AuthenticationToken(oauthUser, null, "test");
        return oAuth2AuthenticationToken;
    }
}
