package net.n2oapp.platform.i18n;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Тестирование интернационализации
 */
@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(classes = I18nTest.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class I18nTest {
    private @Autowired Messages messages;

    /**
     * Проверка, что {@link Messages} локализуют сообщения с кодом и без, с параметрами и без.
     */
    @Test
    public void messages() {
        assertThat(messages.getMessage("test1"), is("Тест"));
        assertThat(messages.getMessage("test2", "раз", 2), is("Тест раз и 2"));
        assertThat(messages.getMessage("undefined"), is("undefined"));
        assertThat(messages.getMessage("Сообщение {0} и {1}", "раз", 2), is("Сообщение раз и 2"));

        Message message = new Message("test2").set("раз").set(2);
        assertThat(messages.getMessage(message), is("Тест раз и 2"));
    }

    /**
     * Проверка локализации исключений {@link MessageException} и подключения глобального {@link org.springframework.context.MessageSource}
     */
    @Test
    public void exceptions() {
        try {
            throw new MessageException("test1");
        } catch (Exception e) {
            assertThat(messages.getMessage(e), is("Тест"));
        }
        try {
            throw new MessageException("test2").set("раз").set(2);
        } catch (Exception e) {
            assertThat(messages.getMessage(e), is("Тест раз и 2"));
        }
        try {
            throw new MessageException();//unexpected
        } catch (Exception e) {
            assertThat(messages.getMessage(e), is("Внутренняя ошибка приложения"));
        }
        try {
            throw new IllegalStateException("test1");
        } catch (Exception e) {
            assertThat(messages.getMessage(e), is("Тест"));
        }
        Message message = new Message("test2").set("раз").set(2);
        try {
            throw new MessageException(message);
        } catch (Exception e) {
            assertThat(messages.getMessage(e), is("Тест раз и 2"));
        }
    }
}
