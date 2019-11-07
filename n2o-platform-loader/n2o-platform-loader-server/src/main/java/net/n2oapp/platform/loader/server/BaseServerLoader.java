package net.n2oapp.platform.loader.server;

import net.n2oapp.platform.loader.server.repository.EntityIdentifier;
import net.n2oapp.platform.loader.server.repository.LoaderMapper;
import net.n2oapp.platform.loader.server.repository.SubjectFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Базовая версия серверного загрузчика данных через репозиторий Spring Data
 *
 * @param <M>  Тип модели
 * @param <E>  Тип сущности
 * @param <ID> Тип идентификатора сущности
 */
abstract public class BaseServerLoader<M, E, ID> implements ServerLoader<M> {
    private CrudRepository<E, ID> repository;
    private LoaderMapper<M, E> mapper;
    private SubjectFilter<E> filter;
    private EntityIdentifier<E, ID> identifier;

    /**
     *  Сохранение данных
     */
    @Value("${n2o.loader.server.createRequired}")
    private boolean createRequired;
    /**
     *  Обновление данных
     */
    @Value("${n2o.loader.server.updateRequired}")
    private boolean updateRequired;
    /**
     *  Удаление данных
     */
    @Value("${n2o.loader.server.deleteRequired}")
    private boolean deleteRequired;

    /**
     * Конструктор серверного загрузчика данных с удаленим устаревших.
     * Если передан фильтр по владельцу, то устаревшие данные этого владельца при загрузке будут удалены.
     *
     * @param mapper     Конвертер
     * @param repository Репозиторий
     * @param filter     Фильтр по владельцу
     */
    public BaseServerLoader(CrudRepository<E, ID> repository,
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
    public BaseServerLoader(CrudRepository<E, ID> repository,
                            LoaderMapper<M, E> mapper) {
        this(repository, mapper, null, null);
    }

    @Override
    @Transactional
    public void load(List<M> data, String subject) {
        List<E> fresh = map(data, subject);
        save(fresh);
        if (deleteRequired)
            delete(fresh, subject);
    }

    /**
     * Конвертаций входных моделей в сущности
     * @param data    Список моделей
     * @param subject Владелец данных
     * @return        Список сущностей
     */
    protected List<E> map(List<M> data, String subject) {
        List<E> fresh = new ArrayList<>();
        for (M model : data) {
            E entity = mapper.map(model, subject);
            fresh.add(entity);
        }
        return fresh;
    }

    /**
     * Сохранение / обновление записей в БД
     * @param fresh Список сущностей
     */
    protected void save(List<E> fresh) {
//        List<E> updated = new ArrayList<>();
//        List<E> created = new ArrayList<>();
//
//
//        for (E entity : fresh) {
//            if (repository.)
//        }
//
//        if (createRequired)
//            repository.saveAll(created);
//        if (updateRequired)
//            repository.saveAll(updated);
        repository.saveAll(fresh);
    }

    /**
     * Удаление устаревших записей из БД
     * @param loaded  Список сущностей
     * @param subject Владелец данных
     */
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
