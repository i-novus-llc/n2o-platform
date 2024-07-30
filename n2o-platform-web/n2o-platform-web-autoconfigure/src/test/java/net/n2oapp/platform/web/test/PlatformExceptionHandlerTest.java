package net.n2oapp.platform.web.test;

import net.n2oapp.framework.api.exception.N2oException;
import net.n2oapp.framework.api.exception.N2oUserException;
import net.n2oapp.framework.api.exception.ValidationMessage;
import net.n2oapp.framework.api.metadata.global.dao.object.field.ObjectSimpleField;
import net.n2oapp.framework.api.metadata.local.CompiledObject;
import net.n2oapp.framework.api.metadata.local.CompiledQuery;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestMessage;
import net.n2oapp.platform.web.autoconfigure.PlatformExceptionHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Тесты класса {@link PlatformExceptionHandler}
 */
class PlatformExceptionHandlerTest {
    /**
     * Тест обработки пользовательских сообщений из исключений выбрасываемых RestTemplate
     */
    @Test
    void handleUserMessageFromRestTemplate() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        String body = "{\"message\":\"Wrong data\"}";
        N2oException e = handler.handle((CompiledObject.Operation) null, null,
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                        "Bad request",
                        HttpHeaders.EMPTY,
                        body.getBytes(),
                        StandardCharsets.UTF_8));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("Wrong data"));
    }

    /**
     * Тест обработки пользовательских сообщений из исключений выбрасываемых JaxRs Proxy Client
     */
    @Test
    void handleUserMessageFromJaxRsClient() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        N2oException e = handler.handle((CompiledQuery) null, null,
                new RestException(new RestMessage("Wrong data"), 400));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("Wrong data"));
    }

    /**
     * Тест обработки пользовательских сообщений из исключений UserException
     */
    @Test
    void handleUserMessageFromUserException() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        N2oException e = handler.handle((CompiledQuery) null, null,
                new UserException("Wrong data"));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("Wrong data"));
    }

    /**
     * Тест обработки технических исключений выбрасываемых RestTemplate
     */
    @Test
    void handleStacktraceFromRestTemplate() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        String body = "{\"message\":\"Unexpected error\",\"stackTrace\":" +
                "[\"com.example.test.BusinessException: something went wrong\"," +
                "\"\\tat com.example.test.BusinessService.someMethod(BusinessService.java:123)\"]}";
        N2oException e = handler.handle((CompiledQuery) null, null,
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Bad request",
                        HttpHeaders.EMPTY,
                        body.getBytes(),
                        StandardCharsets.UTF_8));

        assertThat(e, instanceOf(N2oException.class));
        assertThat(e.getCause(), instanceOf(RestException.class));
        assertThat(e.getMessage(), containsString("Unexpected error"));
        assertThat(Stream.of(ExceptionUtils.getStackFrames(e.getCause())).anyMatch(sf ->
                sf.contains("something went wrong")), is(true));
    }

    /**
     * Тест обработки технических исключений выбрасываемых JaxRs Proxy Client
     */
    @Test
    void handleStacktraceFromJaxRsClient() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        RestMessage message = new RestMessage("Unexpected error");
        message.setStackTrace(new String[]{
                "com.example.test.BusinessException: something went wrong",
                "\tat com.example.test.BusinessService.someMethod(BusinessService.java:123)"});
        RestException target = new RestException(message, 500);
        //
        N2oException e = handler.handle((CompiledObject.Operation) null, null, target);
        assertThat(e, instanceOf(N2oException.class));
        assertThat(e.getCause(), instanceOf(RestException.class));
        assertThat(e.getMessage(), containsString("Unexpected error"));
        assertThat(Stream.of(ExceptionUtils.getStackFrames(e.getCause())).anyMatch(sf ->
                sf.contains("something went wrong")), is(true));
    }

    /**
     * Тест обработки пользовательских сообщений из множественных ошибок,
     * выбрасываемых JaxRs Proxy Client для object operation
     */
    @Test
    void handleMultipleErrorsFromJaxRsClientForObject() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();

        ObjectSimpleField inParam1 = new ObjectSimpleField();
        inParam1.setValidationFailKey("create.arg0.name");
        ObjectSimpleField inParam2 = new ObjectSimpleField();
        inParam2.setValidationFailKey("create.arg0.age");
        CompiledObject.Operation operation = new CompiledObject.Operation();
        operation.setInParametersMap(Map.of("name", inParam1, "age", inParam2));

        RestMessage restMessage = new RestMessage(Arrays.asList(
                new RestMessage.ConstraintViolationError("Не должно равняться нулю", "create.arg0.name", null, null, null),
                new RestMessage.Error("Wrong data"),
                new RestMessage.Error("Wrong format"),
                new RestMessage.ConstraintViolationError("Не должно быть отрицательным", "create.arg0.age", null, null, null)
        ));

        N2oException e = handler.handle(operation, null,
                new N2oException(new RestException(restMessage, 400)));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("1) Wrong data\n2) Wrong format\n"));

        List<ValidationMessage> messages = userException.getMessages();
        assertThat(messages.size(), is(2));
        assertThat(messages.get(0).getMessage(), is("Не должно равняться нулю"));
        assertThat(messages.get(0).getFieldId(), is("name"));
        assertThat(messages.get(1).getMessage(), is("Не должно быть отрицательным"));
        assertThat(messages.get(1).getFieldId(), is("age"));
    }

    /**
     * Тест обработки пользовательских сообщений из множественных ошибок,
     * выбрасываемых JaxRs Proxy Client для query
     */
    @Test
    void handleMultipleErrorsFromJaxRsClientForQuery() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        CompiledQuery query = new CompiledQuery();

        RestMessage restMessage = new RestMessage(Arrays.asList(
                new RestMessage.Error("Wrong data"),
                new RestMessage.Error("Wrong format")
        ));

        N2oException e = handler.handle(query, null,
                new N2oException(new RestException(restMessage, 400)));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("1) Wrong data\n2) Wrong format"));
    }
}