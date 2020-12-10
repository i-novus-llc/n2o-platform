package net.n2oapp.platform.web.test;

import net.n2oapp.criteria.dataset.DataSet;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping(value = "/protectedResource", produces = MediaType.APPLICATION_JSON)
    public DataSet protectedResource(HttpServletRequest request) {
        return new DataSet("token", Collections.singletonList(Map.of("auth", request.getHeader("authorization"))));
    }

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON)
    public OAuth2AccessToken tokenEndpoint() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(JwtForwardingTest.refreshedToken);
        token.setTokenType("Bearer");
        return token;
    }
}
