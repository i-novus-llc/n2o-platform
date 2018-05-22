package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.n2oapp.platform.jaxrs.example.api.SomeModel;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;

@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JaxRsServerTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JaxRsServerTest {
    @LocalServerPort
    private int port;

    /**
     * Проверка, что REST сервис был автоматически найден и поднят по пути /api/info.
     */
    @Test
    public void info() {
        Response response = client().path("info").get();
        assertThat(response.getStatus(), equalTo(200));
        String html = response.readEntity(String.class);
        assertThat(html, containsString("Endpoint address"));
        assertThat(html, containsString("Swagger"));
        assertThat(html, containsString("WADL"));
        response = client().path("example").path("search").head();
        assertThat(response.getStatus(), equalTo(200));
    }

    /**
     * Проверка, что REST сервис обрабатывает Pageable параметры.
     */
    @Test
    public void paging() {
        Map<?, ?> page = client().path("example").path("search")
                .query("size", 20).query("page", 2).get(Map.class);
        assertThat(page.get("totalElements"), equalTo(100));
        assertThat(page.get("content"), instanceOf(List.class));
        List<Map<String, Object>> content = (List) page.get("content");
        assertThat(content.size(), equalTo(20));
        assertThat(content.get(0).get("id"), equalTo(40));
    }

    /**
     * Проверка, что REST сервис обрабатывает Pageable параметры по умолчанию.
     */
    @Test
    public void pagingByDefault() {
        Map<?, ?> page = client().path("example").path("search").get(Map.class);
        assertThat(page.get("totalElements"), equalTo(100));
        assertThat(page.get("content"), instanceOf(List.class));
        List<Map<String, Object>> content = (List) page.get("content");
        assertThat(content.size(), equalTo(10));
        assertThat(content.get(0).get("id"), equalTo(0));
    }

    /**
     * Проверка, что REST сервис обрабатывает Sort.Order параметры.
     */
    @Test
    public void sort() {
        Map<?, ?> page = client().path("example").path("search")
                .query("sort", "name,asc", "id,desc")
                .get(Map.class);
        assertThat(page.get("sort"), notNullValue());
        List<Map<String, Object>> sort = (List<Map<String, Object>>) page.get("sort");
        assertThat(sort.size(), equalTo(2));
        assertThat(sort.get(0).get("property"), equalTo("name"));
        assertThat(sort.get(0).get("direction").toString(), equalToIgnoringCase("asc"));
        assertThat(sort.get(1).get("property"), equalTo("id"));
        assertThat(sort.get(1).get("direction").toString(), equalToIgnoringCase("desc"));
    }

    /**
     * Проверка, что REST сервис обрабатывает Page ответ.
     */
    @Test
    public void pageResult() {
        Map<?, ?> page = client().path("example").path("search").get(Map.class);
        assertThat(page.size(), equalTo(2));
        assertThat(page.get("content"), instanceOf(List.class));
        assertThat(page.get("totalElements"), instanceOf(Number.class));
    }

    /**
     * Проверка, что REST сервис обрабатывает Sort ответ.
     */
    @Test
    public void sortResult() {
        Map<?, ?> page = client().path("example").path("search")
                .query("sort", "name,asc").get(Map.class);
        assertThat(page.size(), equalTo(3));
        assertThat(page.get("sort"), instanceOf(List.class));
        List<Map<String, Object>> sort = (List<Map<String, Object>>) page.get("sort");
        assertThat(sort.size(), equalTo(1));
        assertThat(sort.get(0).size(), equalTo(2));
    }


    /**
     * Проверка, что REST сервис обрабатывает List ответ.
     */
    @Test
    public void listResult() {
        List<?> list = client().path("example").path("list").get(List.class);
        assertThat(list.size(), notNullValue());
    }

    /**
     * Проверка, что REST сервис обрабатывает Long ответ (например, идентификатор записи).
     */
    @Test
    public void idResult() {
        Long result = client().path("example").path("count").get(Long.class);
        assertThat(result, equalTo(100L));
    }

    /**
     * Проверка, что REST сервис обрабатывает ответ в виде java модели.
     */
    @Test
    public void singleResult() {
        SomeModel result = client().path("example").path(1).get(SomeModel.class);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(1L));
    }

    /**
     * Проверка, что REST сервис правильно обрабатывает параметры фильтрации.
     */
    @Test
    public void filters() {
        Map<?, ?> page = client().path("example").path("search")
                .query("name", "John")
                .query("date", "2018-03-01T08:00:00Z")
                .get(Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) page.get("content");
        assertThat(content.get(0).get("name"), equalTo("John"));
        assertThat(content.get(0).get("date"), equalTo("2018-03-01T08:00:00Z"));
    }

    /**
     * Проверка, что REST сервис правильно обрабатывает валидации JSR303.
     */
    @Test
    public void validations() {
        Map<String, Object> model = new HashMap<>();
        model.put("name", "");//Имя должно быть задано
        model.put("date", "2030-01-01T12:00:00Z");//Дата не должна быть в будущем
        Response response = client().path("example").post(model);
        assertThat(response.getStatusInfo().getFamily(), equalTo(Response.Status.Family.CLIENT_ERROR));
        Map<?, ?> message = response.readEntity(Map.class);
        assertThat(message, notNullValue());
        assertThat(message.get("errors"), notNullValue());
        assertThat(((List) message.get("errors")).size(), equalTo(2));
    }

    private WebClient client() {
        return WebClient.create("http://localhost:" + port, Collections.singletonList(new JacksonJsonProvider()))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .path("api");
    }
}
