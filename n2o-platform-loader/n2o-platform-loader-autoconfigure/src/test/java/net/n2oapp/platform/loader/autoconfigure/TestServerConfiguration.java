package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.ServerLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static net.n2oapp.platform.loader.server.ServerLoaderCommand.asIterable;
import static net.n2oapp.platform.loader.server.ServerLoaderCommand.asObject;

@Configuration
public class TestServerConfiguration {
    @Bean
    ServerLoaderConfigurer loaders() {
        return runner -> {
            runner.add(asObject(new MyLoader1(), "load1", Object.class));
            runner.add(asIterable(new MyLoader2(), "load2", Object.class));
        };
    }

    static class MyLoader1 implements ServerLoader<Object> {
        @Override
        public void load(Object data, String subject) {
            //run something
        }
    }

    static class MyLoader2 implements ServerLoader<List<Object>> {
        @Override
        public void load(List<Object> data, String subject) {
            //run something
        }
    }
}
