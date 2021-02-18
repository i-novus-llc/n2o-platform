package net.n2oapp.platform.selection.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что join вложенной сущности можно явно сгруппировать с помощью {@link Joiner}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Joined {

    /**
     * @return {@code true}, если вложенные сущности вложенной сущности можно сгруппировать
     */
    boolean withNestedJoiner() default true;

}
