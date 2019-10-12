package net.n2oapp.platform.loader.server.repository;

import net.n2oapp.platform.loader.server.ServerLoader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Серверный загрузчик данных через репозиторий Spring Data
 *
 * @param <M> Тип модели
 * @param <E> Тип сущности
 */
public class RepositoryServerLoader<M, E> implements ServerLoader<List<M>> {
    private CrudRepository<E, ?> repository;
    private LoaderMapper<M, E> mapper;
    private ClientFilter<E> filter;

    /**
     * Конструктор серверного загрузчика данных с удаленим устаревших.
     * Если передан фильтр по владельцу, то устаревшие данные этого владельца при загрузке будут удалены.
     *
     * @param mapper     Конвертер
     * @param repository Репозиторий
     * @param filter     Фильтр по владельцу
     */
    public RepositoryServerLoader(LoaderMapper<M, E> mapper,
                                  CrudRepository<E, ?> repository,
                                  @Nullable ClientFilter<E> filter) {
        this.mapper = mapper;
        this.repository = repository;
        this.filter = filter;
    }

    /**
     * Конструктор серверного загрузчика данных без удаления устаревших.
     *
     * @param mapper     Конвертер
     * @param repository Репозиторий
     */
    public RepositoryServerLoader(LoaderMapper<M, E> mapper,
                                  CrudRepository<E, ?> repository) {
        this(mapper, repository, null);
    }

    @Override
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

    protected void delete(List<E> fresh, String subject) {
        if (filter == null)
            return;
        List<E> old = filter.findAllBySubject(subject);
        List<E> unused = new ArrayList<>();
        for (E entity : old) {
            if (!fresh.contains(entity))
                unused.add(entity);
        }
        repository.deleteAll(unused);
    }
}
