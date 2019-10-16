package net.n2oapp.platform.loader.server;

import java.io.InputStream;

/**
 * Запускатель серверных загрузчиков
 */
public interface ServerLoaderRunner {
    /**
     * Запустить загрузку данных
     *
     * @param subject Владелец данных
     * @param target  Вид данных
     * @param body    Тело
     */
    void run(String subject, String target, InputStream body);
}
