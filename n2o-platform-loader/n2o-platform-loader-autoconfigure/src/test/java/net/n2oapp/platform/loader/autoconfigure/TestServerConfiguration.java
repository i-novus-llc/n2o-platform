package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.ServerLoader;
import net.n2oapp.platform.loader.server.ServerLoaderRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class TestServerConfiguration {

    @Component
    static class MyLoader1 implements ServerLoader<Object> {
        @Override
        public void load(List<Object> data, String subject) {}

        @Override
        public String getTarget() {
            return "load1";
        }

        @Override
        public Class<Object> getDataType() {
            return Object.class;
        }
    }

    @Component
    static class MyLoader2 implements ServerLoader<Object> {
        @Override
        public void load(List<Object> data, String subject) {}

        @Override
        public String getTarget() {
            return "load2";
        }

        @Override
        public Class<Object> getDataType() {
            return Object.class;
        }
    }
}
