package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.BaseServerLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class TestServerConfiguration {

    @Component
    static class MyLoader1 extends BaseServerLoader<Object, Object> {
        @Override
        protected List<Object> map(List<Object> models, String subject) { return null; }

        @Override
        protected void create(List<Object> entities) {}

        @Override
        protected void update(List<Object> entities) {}

        @Override
        protected void delete(List<Object> entities) {}

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
    static class MyLoader2 extends BaseServerLoader<Object, Object> {
        @Override
        protected List<Object> map(List<Object> models, String subject) { return null; }

        @Override
        protected void create(List<Object> entities) {}

        @Override
        protected void update(List<Object> entities) {}

        @Override
        protected void delete(List<Object> entities) {}

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
