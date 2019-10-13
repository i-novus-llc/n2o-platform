package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.ClientLoader;
import net.n2oapp.platform.loader.client.ClientLoaderRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.net.URI;

@Configuration
public class TestClientConfiguration implements ClientLoaderConfigurer {

    @Override
    public void configure(ClientLoaderRunner runner) {
        runner.add("http://localhost:8080/api", "me", "loader1", "test.json");
        runner.add("http://localhost:8081/api", "me", "loader2", "test.xml", MyClientLoader.class);
    }

    @Component
    static class MyClientLoader implements ClientLoader {

        @Override
        public void load(URI server, String subject, String target, Resource file) {
            //do something
        }
    }
}
