package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.ServerLoader;
import net.n2oapp.platform.loader.server.ServerLoaderRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.n2oapp.platform.loader.server.ServerLoaderRoute.asIterable;
import static net.n2oapp.platform.loader.server.ServerLoaderRoute.asObject;

@Configuration
public class TestServerConfiguration {

    static class RoutesConfiguration implements ServerLoaderConfigurer {
        @Override
        public void configure(ServerLoaderRunner runner) {
            runner.add(asObject("load1", Object.class, MyLoader1.class));
            runner.add(asIterable("load2", Object.class, MyLoader2.class));
        }
    }

    @Component
    static class MyLoader1 implements ServerLoader<Object> {
        @Override
        public void load(Object data, String subject) {
            //run something
        }
    }

    @Component
    static class MyLoader2 implements ServerLoader<List<Object>> {
        @Override
        public void load(List<Object> data, String subject) {
            //run something
        }
    }
}
