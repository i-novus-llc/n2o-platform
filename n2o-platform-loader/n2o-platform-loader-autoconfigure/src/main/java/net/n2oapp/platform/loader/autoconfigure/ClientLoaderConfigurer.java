package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.ClientLoaderRunner;

/**
 * Настройщик клиентских загрузчиков
 */
public interface ClientLoaderConfigurer {
    /**
     * Настроить запуск
     * @param runner Запускатель загрузчиков
     */
    void configure(ClientLoaderRunner runner);
}
