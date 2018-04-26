package net.n2oapp.platform.jaxrs.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(JaxRsProxyClientRegistrar.class)
public @interface EnableJaxRsProxyClient {
    /**
     * The alias for {@link #classes()}. Specifies interfaces of JAX-RS resources.
     */
    Class<?>[] value() default {};

    /**
     * Specifies interfaces of JAX-RS resources.
     */
    Class<?>[] classes() default {};

    /**
     * Specifies the package names for class path scanning of JAX-RS resources.
     */
    String[] scanPackages() default {};

    /**
     * The url endpoint rest services.
     */
    String address() default "";
}
