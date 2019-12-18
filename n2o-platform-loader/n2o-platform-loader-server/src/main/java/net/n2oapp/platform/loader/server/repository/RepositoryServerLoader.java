package net.n2oapp.platform.loader.server.repository;

import net.n2oapp.platform.loader.server.BaseServerLoader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Серверный загрузчик данных через репозиторий Spring Data
 *
 * @param <M> Тип модели
 * @param <E> Тип сущности
 * @param <ID> Тип идентификатора сущности
 */
public abstract class RepositoryServerLoader<M, E, ID> extends BaseServerLoader<M, E> {
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

    @Override
    protected List<E> map(List<M> models, String subject) {
        List<E> entities = new ArrayList<>();
        for (M model : models) {
            E entity = mapper.map(model, subject);
            entities.add(entity);
        }
        return entities;
    }

    @Override
    protected List<E> findAllBySubject(String subject) {
        if (filter != null)
            return filter.findAllBySubject(subject);
        else
            return super.findAllBySubject(subject);
    }

    @Override
    protected boolean contains(List<E> entities, E candidate) {
        if (identifier != null) {
            ID candidateId = identifier.identify(candidate);
            for (E entity : entities) {
                if (identifier.identify(entity).equals(candidateId))
                    return true;
            }
            return false;
        } else {
            return super.contains(entities, candidate);
        }
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
    protected void delete(List<E> entities) {
        repository.deleteAll(entities);
    }
}
