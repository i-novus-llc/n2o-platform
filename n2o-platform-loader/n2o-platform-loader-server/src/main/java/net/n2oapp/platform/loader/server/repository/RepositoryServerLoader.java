package net.n2oapp.platform.loader.server.repository;

import net.n2oapp.platform.loader.server.BaseServerLoader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Серверный загрузчик данных через репозиторий Spring Data
 *
 * @param <M> Тип модели
 * @param <E> Тип сущности
 * @param <ID> Тип идентификатора сущности
 */
public abstract class RepositoryServerLoader<M, E, ID> extends BaseServerLoader<M, E, ID> {
    private CrudRepository<E, ID> repository;
    private LoaderMapper<M, E> mapper;
    private SubjectFilter<E> filter;
    private EntityIdentifier<E, ID> identifier;

    /**
     * Конструктор серверного загрузчика данных с удаленим устаревших.
     * Если передан фильтр по владельцу, то устаревшие данные этого владельца при загрузке будут удалены.
     *
     * @param mapper     Конвертер
     * @param repository Репозиторий
     * @param filter     Фильтр по владельцу
     */
    public RepositoryServerLoader(CrudRepository<E, ID> repository,
                                  LoaderMapper<M, E> mapper,
                                  @Nullable SubjectFilter<E> filter,
                                  @Nullable EntityIdentifier<E, ID> identifier) {
        this.repository = repository;
        this.mapper = mapper;
        this.filter = filter;
        this.identifier = identifier;
    }

    /**
     * Конструктор серверного загрузчика данных без удаления устаревших.
     *
     * @param mapper     Конвертер
     * @param repository Репозиторий
     */
    public RepositoryServerLoader(CrudRepository<E, ID> repository,
                                  LoaderMapper<M, E> mapper) {
        this(repository, mapper, null, null);
    }

    @Transactional
    @Override
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

    protected List<E> map(List<M> data, String subject) {
        List<E> entities = new ArrayList<>();
        for (M model : data) {
            E entity = mapper.map(model, subject);
            entities.add(entity);
        }
        return entities;
    }

    @Override
    protected void create(List<E> entities) {
        repository.saveAll(entities);
    }

    @Override
    protected void update(List<E> entities) {
        repository.saveAll(entities);
    }

    @Override
    protected void delete(List<E> loaded, String subject) {
        if (filter == null || identifier == null)
            return;
        Set<ID> fresh = loaded.stream().map(identifier::identify).collect(Collectors.toSet());
        List<E> old = filter.findAllBySubject(subject);
        List<E> unused = new ArrayList<>();
        for (E entity : old) {
            if (!fresh.contains(identifier.identify(entity)))
                unused.add(entity);
        }
        repository.deleteAll(unused);
    }

    public CrudRepository<E, ID> getRepository() {
        return repository;
    }
}
