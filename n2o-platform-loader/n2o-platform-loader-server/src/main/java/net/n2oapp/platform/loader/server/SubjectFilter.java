package net.n2oapp.platform.loader.server;

import java.util.List;

/**
 * Фильтр данных по владельцу
 * @param <T> Тип сущности
 */
@FunctionalInterface
public interface SubjectFilter<T> {
    /**
     * Найти все данные по владельцу
     * @param subject Владелец данных
     * @return Список данных
     */
    List<T> findAllBySubject(String subject);
}
