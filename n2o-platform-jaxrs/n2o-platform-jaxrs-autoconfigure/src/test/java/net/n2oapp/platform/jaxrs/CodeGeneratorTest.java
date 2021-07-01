package net.n2oapp.platform.jaxrs;

import org.junit.Assert;
import org.junit.Test;

public class CodeGeneratorTest {

    /*
     * Проверка того, что сгенерируется код с префиксом
     * */
    @Test
    public void generateWithPrefix() {
        final String prefix = "n2o";

        final String code = CodeGenerator.generate(prefix);

        Assert.assertNotNull(code);
        Assert.assertTrue(code.contains(prefix));
    }

    /*
     * Проверка того, что сгенерируется код без префикса
     * */
    @Test
    public void generateWithoutPrefix() {
        final String code = CodeGenerator.generate(null);

        Assert.assertNotNull(code);
    }
}
