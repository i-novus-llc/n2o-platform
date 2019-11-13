package net.n2oapp.platform.loader.server;

import net.n2oapp.platform.loader.server.repository.RepositoryServerLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Тесты базового лоадера
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class BaseServerLoaderTest {

    @Autowired
    private RepositoryServerLoader<TestModel, TestEntity, String> repositoryLoader;

    @Autowired
    private TestRepository repository;

    @LocalServerPort
    private int port;


    /**
     * Тест {@link RepositoryServerLoader}
     */
    @Test
    public void repositoryLoader() {
        BiConsumer<List<TestModel>, String> loader = repositoryLoader::load;
        repository.deleteAll();
        case1(loader);
        case2(loader);
        case3(loader);
        case4(loader);
        case5(loader);
        case6(loader);
        case7(loader);
        case8(loader);
    }

    /**
     * Вставка двух новых записей, в БД нет записей
     * Настройка запрещает сохранять. Проверка, что сохранения не будет
     */
    private void case1(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setCreateRequired(false);
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(0L));
    }

    /**
     * Вставка двух новых записей, в БД нет записей
     */
    private void case2(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setCreateRequired(true);
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
     * Настройка запрещает обновлять. Проверка, что обновления не будет
     */
    private void case3(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setUpdateRequired(false);
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "nameNew"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code2").orElseThrow().getName(), is("name2"));
    }

    /**
     * Вставка двух записей, обе есть в БД, но одна со старым именем
     */
    private void case4(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setUpdateRequired(true);
        List<TestModel> data = Arrays.asList(
                new TestModel("code1", "name1"),
                new TestModel("code2", "nameNew"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code2").orElseThrow().getName(), is("nameNew"));
    }

    /**
     * Вставка двух записей, в БД две записи, первая будет заменена на новую, вторая - обновлена
     * Настройки запрещают создавать, обновлять, удалять.
     */
    private void case5(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setCreateRequired(false);
        repositoryLoader.setUpdateRequired(false);
        repositoryLoader.setDeleteRequired(false);
        List<TestModel> data = Arrays.asList(
                new TestModel("code3", "name3"),
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code1").isPresent(), is(true));
        assertThat(repository.findById("code2").orElseThrow().getName(), is("nameNew"));
    }

    /**
     * Вставка двух записей, в БД две записи, первая будет заменена на новую, вторая - обновлена
     */
    private void case6(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setCreateRequired(true);
        repositoryLoader.setUpdateRequired(true);
        repositoryLoader.setDeleteRequired(true);
        List<TestModel> data = Arrays.asList(
                new TestModel("code3", "name3"),
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code3").isPresent(), is(true));
        assertThat(repository.findById("code2").orElseThrow().getName(), is("name2"));
    }

    /**
     * Вставка одной записи, в БД две записи, вторая будет удалена
     * Настройка запрещает удалять. Проверка, что удаления не будет
     */
    private void case7(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setDeleteRequired(false);
        List<TestModel> data = Arrays.asList(
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(2L));
        assertThat(repository.findById("code3").isPresent(), is(true));
        assertThat(repository.findById("code2").isPresent(), is(true));
    }

    /**
     * Вставка одной записи, в БД две записи, вторая будет удалена
     */
    private void case8(BiConsumer<List<TestModel>, String> loader) {
        repositoryLoader.setDeleteRequired(true);
        List<TestModel> data = Arrays.asList(
                new TestModel("code2", "name2"));
        loader.accept(data, "me");
        assertThat(repository.count(), is(1L));
        assertThat(repository.findById("code2").isPresent(), is(true));
    }
}
