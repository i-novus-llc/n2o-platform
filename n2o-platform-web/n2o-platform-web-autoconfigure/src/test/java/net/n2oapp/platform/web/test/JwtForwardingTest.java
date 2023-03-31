package net.n2oapp.platform.web.test;

import net.n2oapp.criteria.dataset.DataSet;
import net.n2oapp.framework.api.register.route.RouteInfo;
import net.n2oapp.framework.config.N2oApplicationBuilder;
import net.n2oapp.framework.config.metadata.compile.context.QueryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = JwtForwardingTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=62517",
                "security.oauth2.client.client-id=test",
                "security.oauth2.client.client-secret=test",
                "security.oauth2.client.access-token-uri=http://localhost:${server.port}/token",
                "cxf.jaxrs.client.classes-scan=true",
                "cxf.jaxrs.client.classes-scan-packages=net.n2oapp.platform.web",
                "cxf.jaxrs.client.address=http://localhost:${server.port}/protectedResource"
        })
@SpringBootApplication
class JwtForwardingTest {

    public static final String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
    public static final String refreshedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyZWZyZXNoZWQiOnRydWV9";

    private static final RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    private int port;

    @Autowired
    private N2oApplicationBuilder builder;

    @MockBean
    private OAuth2ClientContext clientContext;

    @BeforeEach
    public void setUp() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
        token.setTokenType("Bearer");
        Mockito.when(clientContext.getAccessToken()).thenReturn(token);
        Mockito.when(clientContext.getAccessTokenRequest()).thenReturn(new DefaultAccessTokenRequest());
    }

    @Test
    void testRestProvider() {
        builder.routes(new RouteInfo("/testRestTemplate", new QueryContext("testRestTemplate")));
        Map result = restTemplate.getForObject("http://localhost:" + port + "/n2o/data/testRestTemplate", Map.class);
        assertThat(((Map) ((List) result.get("list")).get(0)).get("auth"), is("Bearer " + accessToken));
    }

    @Test
    void testJaxRsProxyClient() {
        builder.routes(new RouteInfo("/testJaxRs", new QueryContext("testJaxRs")));
        Map result = restTemplate.getForObject("http://localhost:" + port + "/n2o/data/testJaxRs", Map.class);
        assertThat(((Map) ((List) result.get("list")).get(0)).get("auth"), is("Bearer " + accessToken));
    }

    @Test
    void testJaxRsProxyClientRefreshToken() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
        token.setRefreshToken(new DefaultOAuth2RefreshToken(accessToken));
        token.setExpiration(new Date(0));
        Mockito.when(clientContext.getAccessToken()).thenReturn(token);

        builder.routes(new RouteInfo("/testJaxRs", new QueryContext("testJaxRs")));
        Map result = restTemplate.getForObject("http://localhost:" + port + "/n2o/data/testJaxRs", Map.class);
        assertThat(((Map) ((List) result.get("list")).get(0)).get("auth"), is("Bearer " + refreshedToken));
    }


    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public interface TestRestService {
        @GET
        DataSet getToken();
    }
}
