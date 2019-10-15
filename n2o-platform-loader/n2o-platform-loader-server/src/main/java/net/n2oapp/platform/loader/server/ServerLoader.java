package net.n2oapp.platform.loader.server;

import java.util.List;

/**
 * Серверный загрузчик
 *
 * @param <T> Тип данных
 */
public interface ServerLoader<T> extends LoaderDataInfo<T> {
    /**
     * Загрузить данные
     *
     * @param data    Данные
     * @param subject Владелец данных
     */
    void load(List<T> data, String subject);
}
