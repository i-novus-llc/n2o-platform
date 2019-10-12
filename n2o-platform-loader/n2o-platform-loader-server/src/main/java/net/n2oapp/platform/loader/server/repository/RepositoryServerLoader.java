package net.n2oapp.platform.loader.server.repository;

import net.n2oapp.platform.loader.server.ServerLoader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RepositoryServerLoader<M, E> implements ServerLoader<List<M>> {
    private CrudRepository<E, ?> repository;
    private LoaderMapper<M, E> mapper;
    private ClientFilter<E> filter;

    public RepositoryServerLoader(LoaderMapper<M, E> mapper,
                                  CrudRepository<E, ?> repository) {
        this(mapper, repository, null);
    }

    public RepositoryServerLoader(LoaderMapper<M, E> mapper,
                                  CrudRepository<E, ?> repository,
                                  @Nullable ClientFilter<E> filter) {
        this.mapper = mapper;
        this.repository = repository;
        this.filter = filter;
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
        List<E> deprecated = new ArrayList<>();
        for (E entity : old) {
            if (!fresh.contains(entity))
                deprecated.add(entity);
        }
        repository.deleteAll(deprecated);
    }
}
