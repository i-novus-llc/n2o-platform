package net.n2oapp.platform.selection.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает DTO как тип, поля которого можно выборочно отобразить.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Selective {

    /**
     * Префикс, который используется в сочетании с {@link javax.ws.rs.QueryParam}
     * для предотвращения коллизий имен.
     * Если не указан -- по-умолчанию будет вида {@code camelCaseClassName}
     */
    String prefix() default "";

}
