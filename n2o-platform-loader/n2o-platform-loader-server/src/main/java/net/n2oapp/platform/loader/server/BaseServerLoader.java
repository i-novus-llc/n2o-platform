package net.n2oapp.platform.loader.server;

import net.n2oapp.platform.loader.server.repository.EntityIdentifier;
import net.n2oapp.platform.loader.server.repository.SubjectFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Серверный загрузчик данных
 *
 * @param <M>  Тип модели
 * @param <E>  Тип сущности
 */
public abstract class BaseServerLoader<M, E, ID> implements ServerLoader<M> {

    /**
     * Фильтр по владельцу
     */
    private SubjectFilter<E> filter;

    /**
     * Идентификатор сущности
     */
    private EntityIdentifier<E, ID> identifier;

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


    public BaseServerLoader(SubjectFilter<E> filter, EntityIdentifier<E, ID> identifier) {
        this.filter = filter;
        this.identifier = identifier;
    }

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

    public SubjectFilter<E> getFilter() {
        return filter;
    }

    public EntityIdentifier<E, ID> getIdentifier() {
        return identifier;
    }

    public void load(List data, String subject) {
        List<E> entities = map(data, subject);

        if (filter == null || identifier == null) {
            update(entities);
        } else {
            List<E> created = new ArrayList<>();
            List<E> updated = new ArrayList<>();
            Set<ID> oldIds = filter.findAllBySubject(subject).stream().map(identifier::identify).collect(Collectors.toSet());

            for (E entity : entities) {
                if (oldIds.contains(identifier.identify(entity)))
                    updated.add(entity);
                else
                    created.add(entity);
            }

            if (isCreateRequired())
                create(created);
            if (isUpdateRequired())
                update(updated);
        }

        if (isDeleteRequired())
            delete(entities, subject);
    }

    /**
     * Преобразование списка моделей в список сущностей
     * @param models Список моделей
     * @param subject Владелец данных
     * @return Список сущностей
     */
    protected abstract List<E> map(List<M> models, String subject);

    /**
     * Сохранение записей
     * @param entities Список сущностей
     */
    protected abstract void create(List<E> entities);

    /**
     * Обновление записей
     * @param entities Список сущностей
     */
    protected abstract void update(List<E> entities);

    /**
     * Удаление устаревших записей
     * @param entities  Список сущностей
     * @param subject Владелец данных
     */
    protected abstract void delete(List<E> entities, String subject);
}
