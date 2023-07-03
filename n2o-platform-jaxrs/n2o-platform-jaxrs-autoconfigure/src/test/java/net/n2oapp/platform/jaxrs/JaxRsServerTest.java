package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.n2oapp.platform.jaxrs.api.SomeModel;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.n2oapp.platform.jaxrs.Application.HEADERS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootApplication
@SpringBootTest(classes = JaxRsServerTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JaxRsServerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JacksonJsonProvider jsonProvider;

    @Autowired
    private XmlProvider xmlProvider;

    /**
     * Проверка, что REST сервис был автоматически найден и поднят по пути /api/info.
     */
    @Test
    void info() {
        forEachClient(webClient -> {
            Response response = webClient.path("info").get();
            assertThat(response.getStatus(), equalTo(200));
            String html = response.readEntity(String.class);
            assertThat(html, containsString("Endpoint address"));
            assertThat(html, containsString("Swagger"));
            assertThat(html, containsString("WADL"));
        });
    }

    @Test
    void testSearchHead() {
        forEachClient(webClient -> {
            Response response = webClient.path("example").path("search").head();
            assertThat(response.getStatus(), equalTo(200));
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает Pageable параметры.
     */
    @Test
    void paging() {
        forEachClient(webClient -> {
            Page<Map<String, Object>> page = webClient.path("example").path("search")
                    .query("size", 20).query("page", 2).get(Page.class);
            assertThat(page.getTotalElements(), equalTo(100L));
            assertThat(page.getContent(), instanceOf(List.class));
            List<Map<String, Object>> content = page.getContent();
            assertThat(content.size(), equalTo(20));
            assertThat(content.get(0).get("id").toString(), equalTo("40"));
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает Pageable параметры по умолчанию.
     */
    @Test
    void pagingByDefault() {
        forEachClient(webClient -> {
            Page<Map<String, Object>> page = webClient.path("example").path("search").get(Page.class);
            assertThat(page.getTotalElements(), equalTo(100L));
            assertThat(page.getContent(), instanceOf(List.class));
            List<Map<String, Object>> content = page.getContent();
            assertThat(content.size(), equalTo(10));
            assertThat(content.get(0).get("id").toString(), equalTo("0"));
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает Sort.Order параметры.
     */
    @Test
    void sort() {
        forEachClient(webClient -> {
            Page<Map<String, Object>> page = webClient.path("example").path("search")
                    .query("sort", "name: asc", "id: desc")
                    .get(Page.class);
            assertThat(page.getSort(), notNullValue());
            Sort sort = page.getSort();
            List<Sort.Order> orders = new ArrayList<>();
            for (Sort.Order order : sort)
                orders.add(order);
            assertThat(orders.size(), equalTo(2));
            assertThat(orders.get(0).getProperty(), equalTo("name"));
            assertThat(orders.get(0).getDirection().toString(), equalToIgnoringCase("asc"));
            assertThat(orders.get(1).getProperty(), equalTo("id"));
            assertThat(orders.get(1).getDirection().toString(), equalToIgnoringCase("desc"));
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает Page ответ.
     */
    @Test
    void pageResult() {
        forEachClient(webClient -> {
            Page page = webClient.path("example").path("search").get(Page.class);
            assertThat(page.getContent(), instanceOf(List.class));
            assertThat(page.getTotalElements(), instanceOf(Number.class));
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает Sort ответ.
     */
    @Test
    void sortResult() {
        forEachClient(webClient -> {
            Page<Map<String, Object>> page = webClient.path("example").path("search")
                    .query("sort", "name: asc").get(Page.class);
            Sort sort = page.getSort();
            int numSorts = 0;
            for (Sort.Order ignored : sort)
                numSorts++;
            assertThat(numSorts, equalTo(1));
        });
    }


    /**
     * Проверка, что REST сервис обрабатывает List ответ.
     */
    @Test
    void listResult() {
        forEachClient(webClient -> {
            List<?> list = webClient.path("example").path("list").get(List.class);
            assertThat(list.size(), notNullValue());
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает Long ответ (например, идентификатор записи).
     */
    @Test
    void idResult() {
        forEachClient(webClient -> {
            if (webClient.getHeaders().getFirst(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_XML))
                return; // Мы вызываем метод, возвращающий примитивное значение (Long), которое не может быть десереализовано из XML.
            Long result = webClient.path("example").path("count").get(Long.class);
            assertThat(result, equalTo(100L));
        });
    }

    /**
     * Проверка, что REST сервис обрабатывает ответ в виде java модели.
     */
    @Test
    void singleResult() {
        forEachClient(webClient -> {
            SomeModel result = webClient.path("example").path(1).get(SomeModel.class);
            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(1L));
        });
    }

    /**
     * Проверка, что REST сервис правильно обрабатывает параметры фильтрации.
     */
    @Test
    void filters() {
        forEachClient(webClient -> {
            Page<Map<String, Object>> page = webClient.path("example").path("search")
                    .query("name", "John")
                    .query("date", "2018-03-01T08:00:00.000+00:00")
                    .query("dateEnd", "2018-03-31T08:00:00")
                    .get(Page.class);
            List<Map<String, Object>> content = page.getContent();
            assertThat(content.get(0).get("name"), equalTo("John"));
            assertThat(content.get(0).get("date"), equalTo("2018-03-01T08:00:00.000+00:00"));
            assertThat(content.get(0).get("dateEnd"), equalTo("2018-03-31T08:00:00"));
        });
    }

    /**
     * Проверка, что REST сервис правильно обрабатывает валидации JSR303.
     */
    @Test
    void validations() {
        Map<String, Object> model = new HashMap<>();
        model.put("name", ""); // Имя должно быть задано
        model.put("date", "2030-01-01T12:00:00Z"); // Дата не должна быть в будущем
        forEachClient(webClient -> {
            Response response = webClient.path("example").post(model);
            assertThat(response.getStatusInfo().getFamily(), equalTo(Response.Status.Family.CLIENT_ERROR));
            Map<?, ?> message = response.readEntity(Map.class);
            assertThat(message, notNullValue());
            assertThat(message.get("errors"), notNullValue());
            assertThat(((List) message.get("errors")).size(), equalTo(2));
        });
    }

    @Test
    void testGetById() {
        forEachClient(webClient -> {
            SomeModel model = webClient.path("example").path("{id}", 50).get(SomeModel.class);
            assertNotNull(model);
            assertNotNull(model.getId());
            assertNotNull(model.getName());
            assertNotNull(model.getDate());
            assertNotNull(model.getDateEnd());
        });
    }

    @Test
    void testDefaultContentTypeIsJson() {
        WebClient client = WebClient.create("http://localhost:" + port, List.of(jsonProvider, xmlProvider))
                .accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .path("api");
        Response response = client.path("example").path("{id}", 50).get();
        String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
        assertEquals(MediaType.APPLICATION_JSON, contentType);
    }

    private void forEachClient(Consumer<WebClient> clientConsumer) {
        for (WebClient client : clients()) {
            try {
                clientConsumer.accept(client);
            } catch (Exception e) {
                System.out.println("ERROR AT SUCH HEADERS: " + client.getHeaders());
                throw e;
            }
        }
    }

    private WebClient[] clients() {
        WebClient[] clients = new WebClient[HEADERS.length];
        for (int i = 0; i < clients.length; i++) {
            String accept = HEADERS[i].get(HttpHeaders.ACCEPT);
            String contentType = HEADERS[i].get(HttpHeaders.CONTENT_TYPE);
            clients[i] = WebClient.create("http://localhost:" + port, List.of(jsonProvider, xmlProvider))
                            .accept(accept)
                            .type(contentType)
                            .path("api");
        }
        return clients;
    }

}
