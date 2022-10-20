package net.n2oapp.platform.web;

import net.n2oapp.framework.api.exception.N2oException;
import net.n2oapp.framework.api.exception.N2oUserException;
import net.n2oapp.framework.api.metadata.local.CompiledObject;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestMessage;
import net.n2oapp.platform.web.autoconfigure.PlatformExceptionHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Тесты класса {@link PlatformExceptionHandler}
 */
public class PlatformExceptionHandlerTest {
    /**
     * Тест обработки пользовательских сообщений из исключений выбрасываемых RestTemplate
     */
    @Test
    public void handleUserMessageFromRestTemplate() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        String body = "{\"message\":\"Wrong data\"}";
        N2oException e = handler.handle((CompiledObject.Operation) null, null,
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
                        "Bad request",
                        HttpHeaders.EMPTY,
                        body.getBytes(),
                        Charset.forName("UTF-8")));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("Wrong data"));
    }

    /**
     * Тест обработки пользовательских сообщений из исключений выбрасываемых JaxRs Proxy Client
     */
    @Test
    public void handleUserMessageFromJaxRsClient() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        N2oException e = handler.handle((CompiledObject.Operation) null, null,
                new RestException(new RestMessage("Wrong data"), 400));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("Wrong data"));
    }

    /**
     * Тест обработки пользовательских сообщений из исключений UserException
     */
    @Test
    public void handleUserMessageFromUserException() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        N2oException e = handler.handle((CompiledObject.Operation) null, null,
                new UserException("Wrong data"));
        assertThat(e, instanceOf(N2oUserException.class));
        N2oUserException userException = (N2oUserException) e;
        assertThat(userException.getUserMessage(), is("Wrong data"));
    }

    /**
     * Тест обработки технических исключений выбрасываемых RestTemplate
     */
    @Test
    public void handleStacktraceFromRestTemplate() {
        PlatformExceptionHandler handler = new PlatformExceptionHandler();
        String body = "{\"message\":\"Unexpected error\",\"stackTrace\":" +
                "[\"com.example.test.BusinessException: something went wrong\"," +
                "\"\\tat com.example.test.BusinessService.someMethod(BusinessService.java:123)\"]}";
        N2oException e = handler.handle((CompiledObject.Operation) null, null,
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Bad request",
                        HttpHeaders.EMPTY,
                        body.getBytes(),
                        Charset.forName("UTF-8")));

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
    public void handleStacktraceFromJaxRsClient() {
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
}
