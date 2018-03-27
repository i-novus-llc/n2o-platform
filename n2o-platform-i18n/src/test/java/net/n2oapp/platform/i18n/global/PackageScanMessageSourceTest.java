package net.n2oapp.platform.i18n.global;

import net.n2oapp.platform.i18n.I18nTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PackageScanMessageSourceTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"i18n.global.enabled=true", "i18n.global.package-name=global"})
public class PackageScanMessageSourceTest {

    private @Autowired MessageSource messageSource;

    /**
     * Проверка сканирования базовых имен по пакету для {@link MessageSource}
     */
    @Test
    public void test() {
        // Это сообщение находится в global/test.properties.
        // Оно будет найдено, т.к. включено сканирование базовых имен по пакету "global".
        assertThat(messageSource.getMessage("test.message", null, Locale.getDefault()), is("Глобальное сообщение"));
    }
}
