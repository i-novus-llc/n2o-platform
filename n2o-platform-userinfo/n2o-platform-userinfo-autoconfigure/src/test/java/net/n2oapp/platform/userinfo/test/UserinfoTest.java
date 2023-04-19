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
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {UserinfoTest.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.security.strategy=GLOBAL")
@EnableAutoConfiguration
public class UserinfoTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebClient webClient;

    @Test
    public void userinfoTest() {
        SecurityContextHolder.setContext(new SecurityContextImpl(oAuth2AuthenticationToken()));
        ResponseEntity<Boolean> nextServiceContextCorrect = webClient.get().uri("http://localhost:" + port).retrieve().toEntity(Boolean.class).block();
        assertThat(nextServiceContextCorrect.getBody(), is(true));
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
