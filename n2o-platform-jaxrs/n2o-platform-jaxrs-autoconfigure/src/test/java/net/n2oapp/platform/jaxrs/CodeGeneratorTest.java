package net.n2oapp.platform.jaxrs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CodeGeneratorTest {

    /*
     * Проверка того, что сгенерируется код с префиксом
     * */
    @Test
    void generateWithPrefix() {
        final String prefix = "n2o";

        final String code = CodeGenerator.generate(prefix);

        Assertions.assertNotNull(code);
        Assertions.assertTrue(code.contains(prefix));
    }

    /*
     * Проверка того, что сгенерируется код без префикса
     * */
    @Test
    void generateWithoutPrefix() {
        final String code = CodeGenerator.generate(null);

        Assertions.assertNotNull(code);
    }
}
