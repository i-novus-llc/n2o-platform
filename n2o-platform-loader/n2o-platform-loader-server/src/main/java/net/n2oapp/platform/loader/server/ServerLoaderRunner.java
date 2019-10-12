package net.n2oapp.platform.loader.server;

import java.io.InputStream;

/**
 * Запускатель серверных загрузчиков
 */
public interface ServerLoaderRunner {
    /**
     * Запустить загрузку данных
     *
     * @param subject Субъект
     * @param target  Цель
     * @param body    Тело
     */
    void run(String subject, String target, InputStream body);

    /**
     * Добавить команду запуска загрузчика
     *
     * @param command Команда
     * @return Запускатель серверных загрузчиков
     */
    ServerLoaderRunner add(ServerLoaderCommand command);
}
