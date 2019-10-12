package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.n2oapp.platform.loader.server.repository.RepositoryServerLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class TestApplication {
    @Bean
    JacksonJsonProvider jsonProvider(ObjectMapper objectMapper) {
        return new JacksonJsonProvider(JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }

    @Bean
    RepositoryServerLoader<TestModel, TestEntity> repositoryServerLoader(TestRepository repository) {
        return new RepositoryServerLoader<>(TestMapper::map, repository, repository::findAllByClient);
    }

    @Bean
    ServerLoaderRunner jsonLoaderEngine(ObjectMapper objectMapper,
                                      RepositoryServerLoader<TestModel, TestEntity> repositoryServerLoader) {
        return new JsonLoaderRunner(objectMapper)
                .add(ServerLoaderCommand.asIterable(repositoryServerLoader, "test", TestModel.class));
    }
}
