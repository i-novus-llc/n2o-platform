package net.n2oapp.platform.loader.server;

import net.n2oapp.platform.loader.server.repository.RepositoryServerLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Тесты лоадеров
 */
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class LoaderServerTest {
    @Autowired
    private SimpleServerLoader simpleLoader;
    @Autowired
    private RepositoryServerLoader<TestModel, TestEntity, String> repositoryLoader;
    @Autowired
    private TestRepository repository;
    @LocalServerPort
    private int port;

    /**
     * Тест Api {@link ServerLoader}
     */
    @Test
    public void simpleLoader() {
        BiConsumer<List<TestModel>, String> loader = (data, subject) -> {
            simpleLoader.load(data, subject);
        };
        repository.deleteAll();
        case1(loader);
        case2(loader);
        case3(loader);
        case4(loader);
        case5(loader);
    }

    /**
     * Тест {@link RepositoryServerLoader}
     */
    @Test
    public void repositoryLoader() {
        BiConsumer<List<TestModel>, String> loader = (data, subject) -> {
            repositoryLoader.load(data, subject);
        };
        repository.deleteAll();
        case1(loader);
        case2(loader);
        case3(loader);
        case4(loader);
        case5(loader);
    }

    /**
     * Тест {@link ServerLoaderRestService}
     */
    @Test
    public void restLoader() {
        BiConsumer<List<TestModel>, String> loader = (data, subject) -> {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:" + port + "/api/loaders/" + subject + "/load2";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<TestModel>> request = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
        };
        repository.deleteAll();
        case1(loader);
        case2(loader);
        case3(loader);
        case4(loader);
        case5(loader);
    }

    /**
     * Вставка двух новых записей, в БД нет записей
     */
    private void case1(BiConsumer<List<TestModel>, String> loader) {
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code1").isPresent(), is(true));
        assertThat(repository.findById("code2").isPresent(), is(true));
    }

    /**
     * Вставка двух записей, обе есть в БД, но одна со старым именем
     */
    private void case2(BiConsumer<List<TestModel>, String> loader) {
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "nameNew"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code2").orElseThrow().getName(), is("nameNew"));
    }

    /**
     * Вставка трех записей, две есть в БД, третьей нет
     */
    private void case3(BiConsumer<List<TestModel>, String> loader) {
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "nameNew"),
                new TestModel("code3", "name3"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(3L));
        assertThat(repository.findById("code1").isPresent(), is(true));
        assertThat(repository.findById("code2").isPresent(), is(true));
        assertThat(repository.findById("code3").isPresent(), is(true));
    }

    /**
     * Вставка двух записей, в БД три записи, вторая будет обновлена, третья будет удалена
     */
    private void case4(BiConsumer<List<TestModel>, String> loader) {
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code1").isPresent(), is(true));
        assertThat(repository.findById("code2").isPresent(), is(true));
        assertThat(repository.findById("code2").get().getName(), is("name2"));
        assertThat(repository.findById("code3").isEmpty(), is(true));
    }

    /**
     * Вставка двух новых записей клиента "he", в БД 2 записи клиента "me"
     */
    private void case5(BiConsumer<List<TestModel>, String> loader) {
        List<TestModel> data = Arrays.asList(
                new TestModel("code4", "name4"),
                new TestModel("code5", "name5"));
        loader.accept(data, "he");
        assertThat(repository.count(), is(4L));
        assertThat(repository.findAllByClient("he").size(), is(2));
        assertThat(repository.findById("code4").isPresent(), is(true));
        assertThat(repository.findById("code5").isPresent(), is(true));
    }
}
