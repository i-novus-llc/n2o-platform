package net.n2oapp.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import net.n2oapp.platform.feign.SomeFeignClient;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.*;
import net.n2oapp.platform.jaxrs.api.*;
import net.n2oapp.platform.jaxrs.impl.SomeRestImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableFeignClients
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FeignClientTest.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=8765", "cxf.jaxrs.component-scan-packages=com.fasterxml.jackson.jaxrs.json," +
                "net.n2oapp.platform.jaxrs," +
                "net.n2oapp.platform.jaxrs.impl," +
                "net.n2oapp.platform.jaxrs.api," +
                "net.n2oapp.platform.jaxrs.autoconfigure," +
                "org.apache.cxf.jaxrs.validation",
                "n2o.ui.message.stacktrace=true"})
public class FeignClientTest {

    @MockBean
    private OAuth2ClientContext oAuth2ClientContext;

    @Autowired
    private SomeFeignClient client;

    private static final String TEST_TOKEN = "Test_token";

    @Before
    public void setUp() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(TEST_TOKEN);
        token.setTokenType("Bearer");
        Mockito.when(oAuth2ClientContext.getAccessToken()).thenReturn(token);
    }

    /**
     * Проверка, что REST прокси клиент обрабатывает Pageable параметры и параметры фильтрации.
     */
    @Test
    public void pagingAndFiltering() throws Exception {
        SomeCriteria criteria = new SomeCriteria(2, 20);
        criteria.setLikeName("John");
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        criteria.setDateBegin(df.parse("01.01.2018 01:00"));
        criteria.setDateEnd(LocalDateTime.parse("2018-01-12T01:00:00"));
        Page<SomeModel> page = client.search(criteria);
        assertThat(page.getTotalElements(), equalTo(100L));
        assertThat(page.getContent().size(), equalTo(20));
        assertThat(page.getContent().get(0).getId(), equalTo(40L));
        assertThat(page.getContent().get(0).getName(), equalTo("John"));
        assertThat(page.getContent().get(0).getDate(), equalTo(df.parse("01.01.2018 01:00")));
        assertThat(page.getContent().get(0).getDateEnd(), equalTo(LocalDateTime.parse("2018-01-12T01:00:00")));
        Method[] declaredMethods = page.getClass().getDeclaredMethods();
        RestPage<SomeModel> expectedPage = new RestPage<>(page.getContent(), criteria, page.getTotalElements());
        expectedPage.setTotalElements(page.getTotalElements());
        StringBuilder expectedStringOfValues = new StringBuilder();
        StringBuilder actualStringOfValues = new StringBuilder();
        for (Method method : declaredMethods) {
            if(method.getName().startsWith("get") && method.getParameterCount() == 0) {
                expectedStringOfValues.append(method.invoke(expectedPage));
                actualStringOfValues.append(method.invoke(page));
            }
        }
        assertThat(actualStringOfValues.toString(), equalTo(expectedStringOfValues.toString()));
    }

    /**
     * Проверка, что REST прокси клиент обрабатывает Sort.Order параметры.
     */
    @Test
    public void sort() {
        SomeCriteria criteria = new SomeCriteria(1, 10,
                Sort.by(new Sort.Order(ASC, "name"), new Sort.Order(DESC, "date")));
        Page<SomeModel> page = client.search(criteria);
        assertThat(page.getSort(), notNullValue());
        assertThat(page.getSort().getOrderFor("name").getDirection(), equalTo(ASC));
        assertThat(page.getSort().getOrderFor("date").getDirection(), equalTo(DESC));
    }

    /**
     * Проверка обработки JSR303 валидаций от сервера к прокси клиенту.
     */
    @Test
    public void validations() throws ParseException {
        SomeModel model = new SomeModel();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        model.setDate(df.parse("01.01.2050 01:00"));
        try {
            client.create(model);
            fail("Validation didn't work");
        } catch (Exception e) {
            assertThat(e, instanceOf(RestException.class));
            RestException restException = (RestException)e;
            assertThat(restException.getErrors().size(), equalTo(2));
        }
    }

    /**
     * Проверка проброса исключений от сервера к прокси клиенту с сохранением стектрейса.
     */
    @Test
    public void exceptions() {
        try {
            client.update(new SomeModel());//при изменении не задан [id], это вызовет ошибку на сервере
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(RestException.class));
            RestException restException = (RestException) e;
            CodeModel codeModel = CodeModel.buildCode(e.getMessage());
            String message = restException.getMessage();
            codeModel.setCode(message.substring(message.lastIndexOf(" ") + 1));
            assertThat(restException.getMessage(), is(codeModel.getCode()));
            assertThat(restException.getCause(), instanceOf(RemoteException.class));
            assertThat(restException.getCause().getMessage(), is("java.lang.IllegalArgumentException: Field [id] mustn't be null"));
            Optional<StackTraceElement> causeLine = Stream.of(restException.getCause().getStackTrace()).filter(ste ->
                    ste.getMethodName().equals("update")
                            && ste.getClassName().equals(SomeRestImpl.class.getName())
                            && ste.getFileName().equals(SomeRestImpl.class.getSimpleName() + ".java")
                            && ste.getLineNumber() > 0).findAny();
            assertThat(causeLine.isPresent(), is(true));
        }
    }

    /**
     * Проверка локализации сообщений, выбрасываемых исключением i18n {@link UserException}
     */
    @Test
    public void i18n() {
        try {
            client.update(new SomeModel(-1L));//при изменении [id] должен быть положительным числом, это вызовет ошибку на сервере
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(RestException.class));
            RestException restException = (RestException) e;
            assertThat(restException.getMessage(), anyOf(is("Идентификатор -1 должен быть положительным числом"), is("example.idPositive")));
        }
    }

    /**
     * Проверка сериализации/десериализации Page c абстрактным типом
     */
    @Test
    public void pageOfAbstractModel() {
        assertThat(client.searchModel(new SomeCriteria()).getContent().get(0), instanceOf(StringModel.class));
    }

    /**
     * Проверка списка ошибок
     */
    @Test
    public void userExceptionsWithMessageList() {
        try {
            client.throwErrors();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(RestException.class));
            RestException restException = (RestException) e;
            assertThat(restException.getErrors().size(), equalTo(3));
            List<String> errorTextList = restException.getErrors().stream().map(RestMessage.Error::getMessage).collect(Collectors.toList());
            assertThat(errorTextList, anyOf(hasItems("Ошибка пользователя раз", "Ошибка пользователя два", "Другая ошибка пользователя"), hasItems("user.error1", "user.error1", "user.error2")));
        }
    }

    @Test
    public void testAuthorization() {
        assertThat(client.authHeader().get("Authorization"), is("Bearer " + TEST_TOKEN));
    }

    @Configuration
    public static class JaxRsClientTestConfig {
        @Bean
        public MapperConfigurer mapperPreparer() {
            return mapper -> {
                mapper.addMixIn(AbstractModel.class, AbstractModelMixin.class);
                mapper.writerFor(new TypeReference<PageImpl<AbstractModel>>() {
                });
            };
        }
    }
}
