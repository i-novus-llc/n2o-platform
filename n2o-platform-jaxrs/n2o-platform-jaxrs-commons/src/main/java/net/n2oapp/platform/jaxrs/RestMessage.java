package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Сообщение, возвращаемое в ответе REST сервиса
 */
public class RestMessage implements Serializable {

    private static final long serialVersionUID = 139886274946702785L;
    private String message;
    private List<? extends BaseError> errors;
    private String[] stackTrace;

    public RestMessage(List<? extends BaseError> errors) {
        this.errors = errors;
    }

    public RestMessage(String message) {
        this.message = message;
    }

    public RestMessage() {
    }

    public String getMessage() {
        return message;
    }

    public List<? extends BaseError> getErrors() {
        return errors;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "t")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Error.class, name = "1"),
            @JsonSubTypes.Type(value = ConstraintViolationError.class, name = "2")
    })
    public abstract static class BaseError implements Serializable {

        private static final long serialVersionUID = -6241752723478340674L;

        private String message;

        public BaseError() {
        }

        public BaseError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Error error = (Error) o;
            return Objects.equals(getMessage(), error.getMessage());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMessage());
        }

    }

    public static class Error extends BaseError {

        public Error() {
        }

        public Error(String message) {
            super(message);
        }

    }

    public static class ConstraintViolationError extends BaseError {

        private static final long serialVersionUID = -1337L;

        private String field;
        private String leafBeanClass;
        private String constraintAnnotation;

        public ConstraintViolationError() {
        }

        public ConstraintViolationError(String field, String message, String leafBeanClass, String constraintAnnotation) {
            super(message);
            this.field = field;
            this.leafBeanClass = leafBeanClass;
            this.constraintAnnotation = constraintAnnotation;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getLeafBeanClass() {
            return leafBeanClass;
        }

        public void setLeafBeanClass(String leafBeanClass) {
            this.leafBeanClass = leafBeanClass;
        }

        public String getConstraintAnnotation() {
            return constraintAnnotation;
        }

        public void setConstraintAnnotation(String constraintAnnotation) {
            this.constraintAnnotation = constraintAnnotation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ConstraintViolationError that = (ConstraintViolationError) o;
            return Objects.equals(getField(), that.getField()) &&
                    Objects.equals(getLeafBeanClass(), that.getLeafBeanClass()) &&
                    Objects.equals(getConstraintAnnotation(), that.getConstraintAnnotation());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getField(), getLeafBeanClass(), getConstraintAnnotation());
        }

    }

}
