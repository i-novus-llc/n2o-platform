package net.n2oapp.platform.loader.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ClientLoaderTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8787));

    @Autowired
    private ClientLoader jsonClientLoader;
    @Autowired
    private ClientLoader simpleClientLoader;
    @Value("test.json")
    private ClassPathResource json;
    @Value("test.txt")
    private ClassPathResource text;

    @Test
    public void jsonLoad() throws URISyntaxException {
        stubFor(post(urlMatching("/load/.*/.*"))
                .willReturn(aResponse()
                        .withStatus(200)));
        jsonClientLoader.load(new URI("http://localhost:8787"), "foo", "bar", json);
        verify(postRequestedFor(urlEqualTo("/load/foo/bar"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("[{\"code\":\"code1\",\"name\":\"name1\"},{\"code\":\"code2\",\"name\":\"name2\"}]")));
    }

    @Test
    public void simpleLoad() throws URISyntaxException {
        stubFor(post(urlMatching("/simple/.*/.*"))
                .willReturn(aResponse()
                        .withStatus(200)));
        simpleClientLoader.load(new URI("http://localhost:8787"), "foo", "bar", text);
        verify(postRequestedFor(urlEqualTo("/simple/foo/bar"))
                .withHeader("Content-Type", containing("text/plain"))
                .withRequestBody(equalTo("Hello")));
    }
}
