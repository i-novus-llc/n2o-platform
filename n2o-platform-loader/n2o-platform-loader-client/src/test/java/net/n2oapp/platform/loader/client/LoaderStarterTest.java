package net.n2oapp.platform.loader.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static net.n2oapp.platform.loader.client.CollectionUtil.listOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class LoaderStarterTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8787));

    @Autowired
    private ClientLoader simpleClientLoader;


    @Test
    public void testFailRetries() throws InterruptedException {
        ClientLoaderRunner runner = new ClientLoaderRunner(listOf(simpleClientLoader));
        runner.add("http://localhost:8787", "sub", "test1", "test.txt", SimpleClientLoader.class);
        stubFor(post(urlMatching("/simple/sub/test1")).willReturn(aResponse().withStatus(500)));

        LoaderStarter starter = new LoaderStarter(runner, 2, 1);
        starter.start();
        assertThat(starter.getReport().getFails().size(), is(1));

        stubFor(post(urlMatching("/simple/sub/test1")).willReturn(aResponse().withStatus(200)));
        TimeUnit.SECONDS.sleep(5);
        assertThat(starter.getReport().getFails().size(), is(0));
    }
}
