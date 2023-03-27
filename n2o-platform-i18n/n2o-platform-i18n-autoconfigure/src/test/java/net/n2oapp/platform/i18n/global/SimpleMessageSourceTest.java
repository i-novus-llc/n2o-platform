package net.n2oapp.platform.i18n.global;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootApplication
@SpringBootTest(classes = SimpleMessageSourceTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"i18n.global.enabled=false"})
class SimpleMessageSourceTest {
    private @Autowired MessageSource messageSource;

    /**
     * Проверка отключения сканирования базовых имен для {@link MessageSource}
     */
    @Test
    void test() {
        try {
            // Это сообщение не будет найдено, т.к. оно находится в пакете "global",
            // а подключен только messages.properties.
            messageSource.getMessage("test.message", null, Locale.forLanguageTag("ru"));
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NoSuchMessageException.class));
        }
    }
}
