package net.n2oapp.platform.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.criteria.dataset.DataSet;
import net.n2oapp.framework.api.criteria.N2oPreparedCriteria;
import net.n2oapp.framework.api.data.QueryExceptionHandler;
import net.n2oapp.framework.api.exception.N2oException;
import net.n2oapp.framework.api.exception.N2oUserException;
import net.n2oapp.framework.api.exception.ValidationMessage;
import net.n2oapp.framework.api.metadata.global.dao.object.field.ObjectSimpleField;
import net.n2oapp.framework.api.metadata.local.CompiledObject;
import net.n2oapp.framework.api.metadata.local.CompiledQuery;
import net.n2oapp.framework.engine.data.N2oOperationExceptionHandler;
import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.i18n.UserException;
import net.n2oapp.platform.jaxrs.RestException;
import net.n2oapp.platform.jaxrs.RestMessage;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Получение пользовательских сообщений и стектрейса ошибок от REST сервисов
 */
public class PlatformExceptionHandler extends N2oOperationExceptionHandler implements QueryExceptionHandler {
    private Messages messages;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public N2oException handle(CompiledObject.Operation operation, DataSet dataSet, Exception e) {
        if (isMultipleErrorsException(e))
            return handleMultipleErrorsException(operation, e);
        N2oException exception = handle(e);
        if (exception != null)
            return exception;
        return super.handle(operation, dataSet, e);
    }

    @Override
    public N2oException handle(CompiledQuery compiledQuery, N2oPreparedCriteria n2oPreparedCriteria, Exception e) {
        if (isMultipleErrorsException(e))
            return handleMessageErrorsException(((RestException) e.getCause()).getErrors());
        N2oException exception = handle(e);
        if (exception != null)
            return exception;
        if (e instanceof N2oException n2oException)
            return n2oException;
        return new N2oException(e);
    }

    void setMessages(Messages messages) {
        this.messages = messages;
    }

    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private N2oException handle(Exception e) {
        N2oException handled = handleJaxRsException(e);
        if (handled != null)
            return handled;
        handled = handleRestClientException(e);
        if (handled != null)
            return handled;
        return handleUserException(e);
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
        return (userException != null) ? new N2oUserException(getMessage(userException)) : null;
    }

    private String getMessage(UserException userException) {
        if (messages != null) {
            return userException.getArgs() == null
                    ? messages.getMessage(userException.getMessage())
                    : messages.getMessage(userException.getMessage(), userException.getArgs());
        }

        return userException.getMessage();
    }

    private N2oException handleRestClientException(Exception e) {
        HttpStatusCodeException restClientException = unwrapEx(e, HttpStatusCodeException.class);
        if (restClientException != null) {
            RestMessage message;
            try {
                message = objectMapper.readValue(restClientException.getResponseBodyAsByteArray(), RestMessage.class);
            } catch (IOException e1) {
                N2oException n2oException = new N2oException(e);
                n2oException.addSuppressed(e1);
                return n2oException;
            }
            if (restClientException.getStatusCode().is4xxClientError() && message != null) {

                return CollectionUtils.isEmpty(message.getErrors())
                        ? new N2oUserException(message.getMessage())
                        : handleMessageErrorsException(message.getErrors());

            } else if (restClientException.getStatusCode().is5xxServerError() && message != null) {
                return new N2oException(new RestException(message, restClientException.getStatusCode().value()));
            }
        }
        return null;
    }

    private <T extends Exception> T unwrapEx(Throwable e, Class<T> exClass) {
        if (exClass.isAssignableFrom(e.getClass())) {
            //noinspection unchecked
            return (T) e;
        } else if (e.getCause() != null) {
            return unwrapEx(e.getCause(), exClass);
        } else
            return null;
    }

    private boolean isMultipleErrorsException(Exception e) {
        if (e instanceof N2oException && e.getCause() instanceof RestException restException) {
            return restException.getErrors() != null;
        }
        return false;
    }

    private N2oException handleMessageErrorsException(List<? extends RestMessage.BaseError> errors) {
        String message = IntStream
                .rangeClosed(1, errors.size())
                .mapToObj(i -> i + ") " + errors.get(i - 1).getMessage())
                .collect(joining("\n"));
        return new N2oUserException(message);
    }

    private N2oException handleMultipleErrorsException(CompiledObject.Operation operation, Exception e) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        Map<String, String> fieldIdByValidationFailKey = operation.getInParametersMap().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ObjectSimpleField objectSimpleField && objectSimpleField.getValidationFailKey() != null)
                .collect(Collectors.toMap(entry -> ((ObjectSimpleField) entry.getValue()).getValidationFailKey(), Map.Entry::getKey));

        int counter = 1;
        for (RestMessage.BaseError error : ((RestException) e.getCause()).getErrors()) {
            if (error instanceof RestMessage.ConstraintViolationError constraintViolationError)
                validationMessages.add(new ValidationMessage(
                        error.getMessage(),
                        fieldIdByValidationFailKey.get(constraintViolationError.getField()),
                        null
                ));
            else
                message.append(counter++).append(") ").append(error.getMessage()).append("\n");
        }
        return validationMessages.isEmpty() ? new N2oUserException(message.toString()) :
                new N2oUserException(message.toString(), null, validationMessages);
    }
}
