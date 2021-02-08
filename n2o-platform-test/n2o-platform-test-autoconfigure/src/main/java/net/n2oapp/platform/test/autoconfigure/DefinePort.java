package net.n2oapp.platform.test.autoconfigure;

import org.springframework.test.annotation.DirtiesContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@DirtiesContext
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefinePort {
}
