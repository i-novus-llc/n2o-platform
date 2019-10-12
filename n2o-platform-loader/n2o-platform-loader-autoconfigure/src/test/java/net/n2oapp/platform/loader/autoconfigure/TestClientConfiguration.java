package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.ClientLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.net.URI;

@Configuration
public class TestClientConfiguration {
    @Bean
    ClientLoaderConfigurer loaders() {
        return (runner) -> {
            runner.add("http://localhost:8080/api", "me", "loader1", "test.json")
                    .add("http://localhost:8080/api", "me", "loader2", "test.xml", MyClientLoader.class);
        };
    }

    @Component
    static class MyClientLoader implements ClientLoader {

        @Override
        public void load(URI server, String subject, String target, Resource file) {
            //do something
        }
    }
}
