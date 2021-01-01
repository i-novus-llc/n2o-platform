package net.n2oapp.platform.selection.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks Java bean as target for selection
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Selective {

    /**
     * Prefix to be used in conjunction with {@link javax.ws.rs.QueryParam} to prevent name collisions.
     * If not specified -- defaults to {@code camelCaseClassName}
     */
    String prefix() default "";

}
