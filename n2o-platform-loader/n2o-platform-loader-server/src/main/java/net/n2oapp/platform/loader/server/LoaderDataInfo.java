package net.n2oapp.platform.loader.server;

/**
 * Информация о загружаемых данных
 * @param <T> Тип данных
 */
public interface LoaderDataInfo<T> {
    /**
     * @return Цель загрузки
     */
    String getTarget();

    /**
     * @return Класс данных
     */
    Class<T> getDataType();
}
