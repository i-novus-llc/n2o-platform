package net.n2oapp.platform.loader.server.repository;

/**
 * Конвертация модели в сущность
 *
 * @param <M> Тип модели
 * @param <E> Тип сущности
 */
@FunctionalInterface
public interface LoaderMapper<M, E> {
    /**
     * Конвертировать модуль в сущность
     *
     * @param model   Модель
     * @param subject Владелец данных
     * @return Сущность
     */
    E map(M model, String subject);
}
