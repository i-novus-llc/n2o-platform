package net.n2oapp.platform.loader.server.repository;

import net.n2oapp.platform.loader.server.ServerLoader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Серверный загрузчик данных через репозиторий Spring Data
 *
 * @param <M> Тип модели
 * @param <E> Тип сущности
 */
public abstract class RepositoryServerLoader<M, E, ID> implements ServerLoader<M> {
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
        this.mapper = mapper;
        this.repository = repository;
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
    @Transactional
    public void load(List<M> data, String subject) {
        List<E> fresh = map(data, subject);
        save(fresh);
        delete(fresh, subject);
    }

    protected List<E> map(List<M> data, String subject) {
        List<E> fresh = new ArrayList<>();
        for (M model : data) {
            E entity = mapper.map(model, subject);
            fresh.add(entity);
        }
        return fresh;
    }

    protected void save(List<E> fresh) {
        repository.saveAll(fresh);
    }

    protected void delete(List<E> loaded, String subject) {
        if (filter == null || identifier == null)
            return;
        List<ID> fresh = loaded.stream().map(identifier::identify).collect(Collectors.toList());
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
