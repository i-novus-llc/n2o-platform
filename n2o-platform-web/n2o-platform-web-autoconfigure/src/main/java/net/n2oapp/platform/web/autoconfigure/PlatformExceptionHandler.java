package net.n2oapp.platform.web.autoconfigure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.criteria.dataset.DataSet;
import net.n2oapp.framework.api.criteria.N2oPreparedCriteria;
import net.n2oapp.framework.api.data.QueryExceptionHandler;
import net.n2oapp.framework.api.exception.N2oException;
import net.n2oapp.framework.api.exception.N2oUserException;
import net.n2oapp.framework.api.metadata.local.CompiledObject;
import net.n2oapp.framework.api.metadata.local.CompiledQuery;
import net.n2oapp.framework.engine.data.N2oOperationExceptionHandler;
import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestMessage;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Получение пользовательских сообщений и стектрейса ошибок от REST сервисов
 */
public class PlatformExceptionHandler extends N2oOperationExceptionHandler implements QueryExceptionHandler {
    private Messages messages;

    @Override
    public N2oException handle(CompiledObject.Operation operation, DataSet dataSet, Exception e) {
        N2oException exception = handle(e);
        if (exception != null) return exception;
        return super.handle(operation, dataSet, e);
    }

    @Override
    public N2oException handle(CompiledQuery compiledQuery, N2oPreparedCriteria n2oPreparedCriteria, Exception e) {
        N2oException exception = handle(e);
        if (exception != null) return exception;
        if (e instanceof N2oException)
            return (N2oException) e;
        return new N2oException(e);
    }

    void setMessages(Messages messages) {
        this.messages = messages;
    }

    private N2oException handle(Exception e) {
        N2oException handled = handleJaxRsException(e);
        if (handled != null)
            return handled;
        handled = handleRestClientException(e);
        if (handled != null)
            return handled;
        handled = handleUserException(e);
        if (handled != null)
            return handled;
        return null;
    }

    private N2oException handleJaxRsException(Exception e) {
        RestException jaxRsException = unwrapEx(e, RestException.class);
        if (jaxRsException != null) {
            if (jaxRsException.getResponseStatus() >= 400 && jaxRsException.getResponseStatus() < 500) {
                return new N2oUserException(jaxRsException.getMessage());
            } else {
                return new N2oException(jaxRsException);
            }
        }
        return null;
    }

    private N2oException handleUserException(Exception e) {
        UserException userException = unwrapEx(e, UserException.class);
        if (userException != null)
            return new N2oUserException(messages != null ?
                    messages.getMessage(userException.getMessage()) :
                    userException.getMessage());
        return null;
    }

    private N2oException handleRestClientException(Exception e) {
        HttpStatusCodeException restClientException = unwrapEx(e, HttpStatusCodeException.class);
        if (restClientException != null) {
            String body = restClientException.getResponseBodyAsString();
            JsonNode node;
            try {
                node = new ObjectMapper().readTree(body);
            } catch (IOException e1) {
                N2oException n2oException = new N2oException(e);
                n2oException.addSuppressed(e1);
                return n2oException;
            }
            String message = node != null && node.get("message") != null ? node.get("message").asText() : null;
            if (restClientException.getStatusCode().is4xxClientError() && message != null) {
                return new N2oUserException(message);
            } else if (restClientException.getStatusCode().is5xxServerError() && message != null) {
                RestMessage restMessage = new RestMessage(message);
                if (node.get("stackTrace") != null) {
                    List<String> stackTrace = new ArrayList<>();
                    for (JsonNode jsonNode : node.get("stackTrace")) {
                        stackTrace.add(jsonNode.asText());
                    }
                    restMessage.setStackTrace(stackTrace.toArray(new String[0]));
                }
                return new N2oException(new RestException(restMessage, restClientException.getRawStatusCode(), e));
            }
        }
        return null;
    }

    private <T extends Exception> T unwrapEx(Throwable e, Class<T> exClass) {
        if (e.getClass().equals(exClass)) {
            //noinspection unchecked
            return (T) e;
        } else if (e.getCause() != null) {
            return unwrapEx(e.getCause(), exClass);
        } else
            return null;
    }

}
