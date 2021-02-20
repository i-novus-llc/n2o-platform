package net.n2oapp.platform.selection.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.function.Function;

@SpringBootApplication(scanBasePackageClasses = Application.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public static <E1, E2> E2 mapNullable(E1 e1, Function<? super E1, ? extends E2> mapper) {
        return e1 == null ? null : mapper.apply(e1);
    }

}
