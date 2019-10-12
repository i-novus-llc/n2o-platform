package net.n2oapp.platform.loader.server.repository;

import java.util.List;

/**
 * Фильтр данных по владельцу
 * @param <T> Тип сущности
 */
@FunctionalInterface
public interface ClientFilter<T> {
    /**
     * Найти все данные по владельцу
     * @param subject Владелец данных
     * @return Список данных
     */
    List<T> findAllBySubject(String subject);
}
