package net.n2oapp.platform.loader.server;

import java.util.List;

/**
 * Серверный загрузчик данных
 *
 * @param <M>  Тип модели
 * @param <E>  Тип сущности
 */
public abstract class BaseServerLoader<M, E> implements ServerLoader<M> {

    /**
     *  Сохранение данных
     */
    private boolean createRequired = true;
    /**
     *  Обновление данных
     */
    private boolean updateRequired = true;
    /**
     *  Удаление данных
     */
    private boolean deleteRequired = true;


    public boolean isCreateRequired() {
        return createRequired;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public boolean isDeleteRequired() {
        return deleteRequired;
    }

    public void setCreateRequired(boolean createRequired) {
        this.createRequired = createRequired;
    }

    public void setUpdateRequired(boolean updateRequired) {
        this.updateRequired = updateRequired;
    }

    public void setDeleteRequired(boolean deleteRequired) {
        this.deleteRequired = deleteRequired;
    }

    /**
     * Сохранение записей
     * @param fresh Список сущностей
     */
    protected abstract void create(List<E> fresh);

    /**
     * Обновление записей
     * @param fresh Список сущностей
     */
    protected abstract void update(List<E> fresh);

    /**
     * Удаление устаревших записей
     * @param loaded  Список сущностей
     * @param subject Владелец данных
     */
    protected abstract void delete(List<E> loaded, String subject);
}
