package net.n2oapp.platform.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.n2oapp.platform.security.api.FeignJwtHeaderInterceptorRestClient;
import net.n2oapp.platform.security.autoconfigure.FeignJwtHeaderInterceptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootApplication
@EnableFeignClients
@EnableResourceServer
@ComponentScan({"net.n2oapp.platform.jaxrs.impl", "net.n2oapp.platform.security.autoconfigure"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FeignJwtHeaderInterceptorTest.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"n2o.platform.security.key-set-uri=http://localhost:8787/auth/certs",
                "server.port=8765", "n2o.platform.security.check-token-expired=false", "n2o.platform.security.resource-id=test",
                "n2o.platform.security.check-aud=false",
                "cxf.jaxrs.component-scan-packages=com.fasterxml.jackson.jaxrs.json," +
                        "net.n2oapp.platform.jaxrs," +
                        "net.n2oapp.platform.jaxrs.impl," +
                        "net.n2oapp.platform.jaxrs.api," +
                        "net.n2oapp.platform.jaxrs.autoconfigure"})
public class FeignJwtHeaderInterceptorTest {

    @Autowired
    private FeignJwtHeaderInterceptorRestClient client;

    private static final String TOKEN_VALUE = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkZkRGYlJkU3FDZlVkMGV" +
            "vTXRQV0UzVXk2bko5UFhHRDFFN2Q3MXE5c1pNIn0.eyJqdGkiOiJiNmZjZWZhMC0yZjc1LTRlNTMtYTNjMS02M2ExZjE2ZTYzNjciLCJl" +
            "eHAiOjE1NjMyNTg0MTMsIm5iZiI6MCwiaWF0IjoxNTYzMjU4MTEzLCJpc3MiOiJodHRwOi8vMTcyLjE2LjEuMTMwOjg4ODgvYXV0aC9yZ" +
            "WFsbXMvTVBTIiwiYXVkIjoibXBzIiwic3ViIjoiNjRjMDQ1MDgtMWU2Mi00OGQ3LTk2ZjMtNjc3YWE1NzVmNTNmIiwidHlwIjoiQmVhcm" +
            "VyIiwiYXpwIjoibXBzIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiYzcyZjc2YzQtYWM0ZC00ZjU4LWFmOTUtNDA4MzU4ZTF" +
            "iYzJlIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6W10sInJlc291cmNlX2FjY2VzcyI6e30sIm5hbWUiOiJUZXN0IFRlc3QiLCJn" +
            "aXZlbl9uYW1lIjoiVGVzdCIsImZhbWlseV9uYW1lIjoiVGVzdCIsImVtYWlsIjoidG8uaXJ5YWJvdkBnbWFpbC5jb20iLCJ1c2VybmFtZ" +
            "SI6InRlc3QifQ.AQpCJEQZnAews_F_-VBxIiSUMYmNerx_YulUvOAC6YTRWVVlt4BuuKNHS-0i1RURum5x5C7uDcry59r-3Cil8LRBmms" +
            "KUWNoqoWxr4H2Hfny0eFw8rlLwZeDdV7C-jvpO8Z3FTTHk7PybJIBDYG7pLcNStKtpzBeqTVahRt9vxQKhJ5lb0vdPpKnWtyoRaTTnQ7o" +
            "gNchsSSKfsHpvpkG7Ne_3Rd0JiES80VAH9HA8mCqOpRJ1ic2c-hFdmUvhfXSC0pNGcRzKR5hlk7BC9OX0_s5uk-Qi9kf0S_z5pgsPrJD3" +
            "tt5ey_6UoRXtOL7FCNmrLzTVBOsWu0PTgz0XFdh_w";

    @MockBean
    private OAuth2ClientContext clientContext;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8787));

    @Before
    public void setUp() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(TOKEN_VALUE);
        token.setTokenType("Bearer");
        Mockito.when(clientContext.getAccessToken()).thenReturn(token);
    }

    /**
     * Тест {@link FeignJwtHeaderInterceptor}
     */
    @Test
    public void addJwtTokenToHeaderTest() throws JsonProcessingException {
        assertThat("Bearer " + TOKEN_VALUE, is(client.testJwtTokenHeaderInterceptor()));
    }
}
