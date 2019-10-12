package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.ServerLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TestServerConfiguration {
    @Bean
    ServerLoaderConfigurer loaders() {
        return register -> {
            register.add(new MyLoader1(), "load1", Object.class);
            register.addIterable(new MyLoader2(), "load2", Object.class);
        };
    }

    static class MyLoader1 implements ServerLoader<Object> {
        @Override
        public void load(Object data, String subject) {
            //load something
        }
    }

    static class MyLoader2 implements ServerLoader<List<Object>> {
        @Override
        public void load(List<Object> data, String subject) {
            //load something
        }
    }
}
