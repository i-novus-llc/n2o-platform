package net.n2oapp.platform.loader.server;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовый серверный загрузчик данных
 *
 * @param <M>  Тип модели
 * @param <E>  Тип сущности
 */
public abstract class BaseServerLoader<M, E> extends ServerLoaderSettings<M> implements ServerLoader<M> {

    @Transactional
    public void load(List<M> data, String subject) {
        List<E> fresh = map(data, subject);
        List<E> old = findAllBySubject(subject);
        List<E> entitiesForCreate = new ArrayList<>();
        List<E> entitiesForUpdate = new ArrayList<>();
        List<E> entitiesForDelete = new ArrayList<>();

        for (E entity : fresh) {
            if (contains(old, entity))
                entitiesForUpdate.add(entity);
            else
                entitiesForCreate.add(entity);
        }
        if (isDeleteRequired())
            for (E entity : old) {
                if (!contains(fresh, entity))
                    entitiesForDelete.add(entity);
            }

        if (isCreateRequired() && !entitiesForCreate.isEmpty())
            create(entitiesForCreate);
        if (isUpdateRequired() && !entitiesForUpdate.isEmpty())
            update(entitiesForUpdate);
        if (isDeleteRequired() && !entitiesForDelete.isEmpty())
            delete(entitiesForDelete);
    }

    /**
     * Найти все данные по владельцу
     * @param subject Владелец данных
     * @return Список данных
     */
    protected List<E> findAllBySubject(String subject) {
        return Collections.emptyList();
    }

    /**
     * Содержится ли сущность в списке.
     * Сравнивать нужно не по полной эквивалентности, а по ключевым свойствам.
     * @param entities Список
     * @param candidate Сущность
     * @return Содержится true, не содержится false
     */
    protected boolean contains(List<E> entities, E candidate) {
        return entities.contains(candidate);
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
     * @param entities Список сущностей, которые требуется создать
     */
    protected abstract void create(List<E> entities);

    /**
     * Обновление записей
     * @param entities Список сущностей, которые требуется обновить
     */
    protected abstract void update(List<E> entities);

    /**
     * Удаление устаревших записей
     * @param entities  Список сущностей, которые требуется удалить
     */
    protected abstract void delete(List<E> entities);
}
