package net.n2oapp.platform.test.autoconfigure.pg;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration(TestcontainersPgAutoConfiguration.class)
public @interface EnableTestcontainersPg {
}
