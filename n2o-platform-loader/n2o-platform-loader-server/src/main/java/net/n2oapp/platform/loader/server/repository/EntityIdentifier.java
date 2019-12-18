package net.n2oapp.platform.loader.server.repository;

/**
 * Идентификация сущности
 * @param <T> Тип сущности
 * @param <ID> Тип идентификатора
 */
@FunctionalInterface
public interface EntityIdentifier<T, ID> {
    /**
     * Получить идентификатор сущности
     * @param entity Сущность
     * @return Идентификатор
     */
    ID identify(T entity);
}
