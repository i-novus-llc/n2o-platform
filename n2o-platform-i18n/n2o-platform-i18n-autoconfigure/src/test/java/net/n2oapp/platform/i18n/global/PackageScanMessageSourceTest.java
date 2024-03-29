package net.n2oapp.platform.i18n.global;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootApplication
@SpringBootTest(classes = PackageScanMessageSourceTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"i18n.global.enabled=true", "i18n.global.package-name=global"})
class PackageScanMessageSourceTest {

    private @Autowired MessageSource messageSource;

    /**
     * Проверка сканирования базовых имен по пакету для {@link MessageSource}
     */
    @Test
    void test() {
        // Это сообщение находится в global/test.properties.
        // Оно будет найдено, т.к. включено сканирование базовых имен по пакету "global".
        assertThat(messageSource.getMessage("test.message", null, Locale.forLanguageTag("ru")), is("Глобальное сообщение"));
    }
}
