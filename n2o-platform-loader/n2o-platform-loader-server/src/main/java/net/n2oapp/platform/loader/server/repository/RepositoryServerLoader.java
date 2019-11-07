package net.n2oapp.platform.loader.server.repository;

import net.n2oapp.platform.loader.server.BaseServerLoader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

/**
 * Серверный загрузчик данных через репозиторий Spring Data
 *
 * @param <M> Тип модели
 * @param <E> Тип сущности
 * @param <ID> Тип идентификатора сущности
 */
public abstract class RepositoryServerLoader<M, E, ID> extends BaseServerLoader {
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
        super(repository, mapper, filter, identifier);
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
}
