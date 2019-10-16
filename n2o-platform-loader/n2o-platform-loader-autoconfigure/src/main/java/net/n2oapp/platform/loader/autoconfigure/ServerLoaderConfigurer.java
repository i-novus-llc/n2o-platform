package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.ServerLoaderRunner;

/**
 * Настройщик серверной загрузки
 */
public interface ServerLoaderConfigurer {
    /**
     * Настроить серверные загрузчики
     * @param runner Запускатель загрузчиков на сервере
     */
    void configure(ServerLoaderRunner runner);
}
