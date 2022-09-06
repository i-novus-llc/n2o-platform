package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.core.type.TypeReference;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.api.*;
import net.n2oapp.platform.jaxrs.impl.SomeRestImpl;
import net.n2oapp.platform.jaxrs.seek.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import javax.ws.rs.core.MultivaluedHashMap;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.n2oapp.platform.jaxrs.Application.HEADERS;
import static net.n2oapp.platform.jaxrs.CollectionUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JaxRsClientTest.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
                properties = {"server.port=8423",
                "n2o.ui.message.stacktrace=true"})
public class JaxRsClientTest {

    @Autowired
    @Qualifier("someRestJaxRsProxyClient")//в контексте теста есть 2 бина SomeRest: SomeRestImpl и прокси клиент
    private SomeRest client;

    /**
     * Проверка, что REST прокси клиент обрабатывает Pageable параметры и параметры фильтрации.
     */
    @Test
    public void pagingAndFiltering() {
        forEachHeaderCombination(() -> {
            SomeCriteria criteria = new SomeCriteria(2, 20);
            criteria.setLikeName("John");
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            criteria.setDateBegin(df.parse("01.01.2018 01:00"));
            Page<SomeModel> page = client.search(criteria);
            assertThat(page.getTotalElements(), equalTo(100L));
            assertThat(page.getContent().size(), equalTo(20));
            assertThat(page.getContent().get(0).getId(), equalTo(40L));
            assertThat(page.getContent().get(0).getName(), equalTo("John"));
            assertThat(page.getContent().get(0).getDate(), equalTo(df.parse("01.01.2018 01:00")));
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
        });
    }

    /**
     * Проверка, что REST прокси клиент обрабатывает Sort.Order параметры.
     */
    @Test
    public void sort() {
        forEachHeaderCombination(() -> {
            SomeCriteria criteria = new SomeCriteria(1, 10,
                    Sort.by(new Sort.Order(ASC, "name"), new Sort.Order(DESC, "date")));
            Page<SomeModel> page = client.search(criteria);
            assertThat(page.getSort(), notNullValue());
            assertThat(page.getSort().getOrderFor("name").getDirection(), equalTo(ASC));
            assertThat(page.getSort().getOrderFor("date").getDirection(), equalTo(DESC));
        });
    }

    /**
     * Проверка обработки JSR303 валидаций от сервера к прокси клиенту.
     */
    @Test
    public void validations() {
        forEachHeaderCombination(() -> {
            SomeModel model = new SomeModel();
            model.setStringModel(new StringModel(null));
            try {
                client.create(model);
                fail();
            } catch (Exception e) {
                assertThat(e, instanceOf(RestException.class));
                RestException restException = (RestException)e;
                assertThat(restException.getErrors().size(), equalTo(4));
                for (RestMessage.BaseError error : restException.getErrors()) {
                    assertThat(error, instanceOf(RestMessage.ConstraintViolationError.class));
                    RestMessage.ConstraintViolationError constraintViolationError = (RestMessage.ConstraintViolationError) error;
                    assertNotNull(constraintViolationError.getConstraintAnnotation());
                    assertTrue(
                            SomeModel.class.getName().equals(constraintViolationError.getLeafBeanClass()) ||
                            StringModel.class.getName().equals(constraintViolationError.getLeafBeanClass())
                    );
                    assertEquals("javax.validation.constraints.NotNull", constraintViolationError.getConstraintAnnotation());
                }
            }
        });
    }

    /**
     * Проверка проброса исключений от сервера к прокси клиенту с сохранением стектрейса.
     */
    @Test
    public void exceptions() {
        forEachHeaderCombination(() -> {
            try {
                client.update(new SomeModel());//при изменении не задан [id], это вызовет ошибку на сервере
                fail();
            } catch (Exception e) {
                assertThat(e, instanceOf(RestException.class));
                RestException restException = (RestException) e;
                assertNotNull(restException.getMessage());
                assertThat(restException.getCause(), instanceOf(RemoteException.class));
                assertThat(restException.getCause().getMessage(), is("java.lang.IllegalArgumentException: Field [id] mustn't be null"));
                Optional<StackTraceElement> causeLine = Stream.of(restException.getCause().getStackTrace()).filter(ste ->
                        ste.getMethodName().equals("update")
                                && ste.getClassName().equals(SomeRestImpl.class.getName())
                                && (SomeRestImpl.class.getSimpleName() + ".java").equals(ste.getFileName())
                                && ste.getLineNumber() > 0).findAny();
                assertThat(causeLine.isPresent(), is(true));
            }
        });
    }

    /**
     * Проверка локализации сообщений, выбрасываемых исключением i18n {@link UserException}
     */
    @Test
    public void i18n() {
        forEachHeaderCombination(() -> {
            try {
                client.update(new SomeModel(-1L));//при изменении [id] должен быть положительным числом, это вызовет ошибку на сервере
                fail();
            } catch (Exception e) {
                assertThat(e, instanceOf(RestException.class));
                RestException restException = (RestException) e;
                assertThat(restException.getMessage(), anyOf(is("Идентификатор -1 должен быть положительным числом"), is("example.idPositive")));
            }
        });
    }

    /**
     * Проверка сериализации/десериализации Page c абстрактным типом
     */
    @Test
    public void pageOfAbstractModel() {
        forEachHeaderCombination(() ->
                assertThat(client.searchModel(new SomeCriteria()).getContent().get(0), instanceOf(StringModel.class))
        );
    }

    /**
     * Проверка списка ошибок
     */
    @Test
    public void userExceptionsWithMessageList() {
        forEachHeaderCombination(() -> {
            try {
                client.throwErrors();
                fail();
            } catch (Exception e) {
                assertThat(e, instanceOf(RestException.class));
                RestException restException = (RestException) e;
                assertThat(restException.getErrors().size(), equalTo(3));
                List<String> errorTextList = restException.getErrors().stream().map(RestMessage.BaseError::getMessage).collect(Collectors.toList());
                assertThat(errorTextList, anyOf(hasItems("Ошибка пользователя раз", "Ошибка пользователя два", "Другая ошибка пользователя"), hasItems("user.error1", "user.error1", "user.error2")));
            }
        });
    }

    /**
     * Проверка Set<List> как @QueryParam, тестируется работа ListConverter
     */
    @Test
    public void testSearchByList() {
        forEachHeaderCombination(() -> {
            List<LocalDateTime> expectedList = Arrays.asList(LocalDateTime.now(), LocalDateTime.now().minusDays(2));
            List<LocalDateTime> actual = client.searchBySetOfTypedList(setOf(expectedList));
            Assert.assertEquals(expectedList, actual);
        });
    }

    /**
     * Проверка Set<Map> как @QueryParam, тестируется работа MapConverter
     */
    @Test
    public void testSearchByMap() {
        forEachHeaderCombination(() -> {
            Map<String, String> expectedMap = mapOf("key1", "value1", "key2", "value2");
            Map<String, String> actual = client.searchBySetOfTypedMap(expectedMap);
            Assert.assertEquals(expectedMap, actual);
        });
    }

    @Test
    public void testGetListOfAbstractModels() {
        forEachHeaderCombination(() -> {
            List<AbstractModel<?>> listOfAbstractModels = client.getListOfAbstractModels();
            assertThat(listOfAbstractModels.size(), equalTo(2));
            assertThat(listOfAbstractModels.get(0).getClass(), equalTo(StringModel.class));
            assertThat(listOfAbstractModels.get(1).getClass(), equalTo(IntegerModel.class));
        });
    }

    @Test
    public void testGetListModels() {
        forEachHeaderCombination(() -> {
            List<ListModel> genericList = client.getListModels();
            int i = 0;
            for (ListModel model : genericList) {
                for (Object obj : model.getValue()) {
                    assertThat(obj.getClass(), equalTo(IntegerModel.class));
                    assertThat(((IntegerModel) obj).getValue(), equalTo(i++));
                }
            }
        });
    }

    /**
     * Проверка сериализации/десериализации классов
     * {@link Seekable}, {@link SeekRequest}, {@link SeekedPageImpl}
     */
    @Test
    public void testSeek() {
        forEachHeaderCombination(() -> {
            SeekRequest request = new SeekRequest();
            request.setPivots(SomeRestImpl.EXPECTED_PIVOTS);
            request.setPage(RequestedPageEnum.NEXT);
            request.setSize(1000);
            request.setSort(Sort.by(listOf(Sort.Order.asc("id"), Sort.Order.desc("name"))));
            SeekedPage<String> page = client.searchSeeking(request);
            assertThat(page.getContent(), is(listOf("ok!")));
            assertThat(page.hasNext(), is(true));
            assertThat(page.hasPrev(), is(false));
            request.setSort(Sort.unsorted());
            client.searchSeeking(request);
        });
    }

    private void forEachHeaderCombination(ExceptionalRunnable run) {
        ReflectionUtils.doWithMethods(client.getClass(), method -> {
            for (Map<String, String> params : HEADERS) {
                try {
                    method.invoke(client, new MultivaluedHashMap<>(params));
                    try {
                        run.run();
                    } catch (Exception e) {
                        System.out.println("ERROR AT SUCH HEADERS: " + params);
                        throw e;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("InvocationTargetException", e);
                }
            }
        }, method -> method.getName().equals("headers"));
    }

    private interface ExceptionalRunnable {
        void run() throws Exception;
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
