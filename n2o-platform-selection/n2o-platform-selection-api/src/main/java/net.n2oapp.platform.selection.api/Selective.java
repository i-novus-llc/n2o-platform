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

    String NULL = "null";

    /**
     * Префикс, который используется в сочетании с {@link javax.ws.rs.QueryParam}
     * для предотвращения коллизий имен в параметрах запроса HTTP.
     * Если {@link #NULL} -- по-умолчанию будет вида {@code camelCaseClassName}
     * Так же можно указать пустую строку и тогда параметры запроса будут называться по имени свойства.
     * Это экономит место, но на вас лежит ответственность по предотвращению коллизии имен
     */
    String prefix() default NULL;

}
