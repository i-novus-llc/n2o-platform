package net.n2oapp.platform.loader.client;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WireMockTest(httpPort = 8787)
public class LoaderStarterTest {

    @Autowired
    private ClientLoader simpleClientLoader;


    @Test
    public void testFailRetries() throws InterruptedException {
        ClientLoaderRunner runner = new ClientLoaderRunner(List.of(simpleClientLoader));
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
