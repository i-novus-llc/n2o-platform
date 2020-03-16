package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
class TestApplication {
    @Bean
    JacksonJsonProvider jsonProvider(ObjectMapper objectMapper) {
        return new JacksonJsonProvider(JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }

    @Bean
    TestRepositoryLoader repositoryServerLoader(TestRepository repository) {
        return new TestRepositoryLoader(repository);
    }

    @Bean
    ServerLoaderRunner jsonLoaderRunner(List<ServerLoader> loaders, ObjectMapper objectMapper) {
        return new JsonLoaderRunner(loaders, objectMapper);
    }
}
