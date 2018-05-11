package net.n2oapp.platform.jaxrs;

import java.io.Serializable;
import java.util.List;

/**
 * Сообщение, возвращаемое в ответе REST сервиса
 */
public class RestMessage implements Serializable {
    private String message;
    private List<Error> errors;
    private String[] stackTrace;

    public RestMessage(List<Error> errors) {
        this.errors = errors;
    }

    public RestMessage(String message) {
        this.message = message;
    }

    public RestMessage() {
    }

    public static class Error implements Serializable {
        private String field;
        private String message;

        public Error() {
        }

        public Error(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public String getMessage() {
        return message;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
