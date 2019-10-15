package net.n2oapp.platform.loader.server;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Базовый запускатель загрузчиков
 */
public abstract class BaseLoaderRunner implements ServerLoaderRunner {
    private Map<String, ServerLoader> loaders;

    public BaseLoaderRunner(List<ServerLoader> loaders) {
        this.loaders = loaders.stream()
                .collect(Collectors.toMap(LoaderDataInfo::getTarget, v -> v));
    }

    @Override
    public void run(String subject, String target, InputStream body) {
        ServerLoader loader = find(target);
        List<Object> data = read(body, loader);
        execute(subject, data, loader);
    }

    /**
     * Прочитать данные
     *
     * @param body Поток данных
     * @param info Информация о типе данных
     * @return Данные
     */
    protected abstract List<Object> read(InputStream body, LoaderDataInfo<?> info);

    /**
     * Поиск загрузчика
     * @param target Цель загрузки
     * @return Загрузчик
     */
    protected ServerLoader find(String target) {
        ServerLoader loader = loaders.get(target);
        if (loader == null)
            throw new NoSuchElementException(String.format("Loader for %s not found", target));
        return loader;
    }

    /**
     * Запуск загрузчика
     *
     * @param subject Владелец данных
     * @param data    Данные
     * @param loader  Загрузчик
     */
    @SuppressWarnings("unchecked")
    protected void execute(String subject, List<Object> data, ServerLoader loader) {
        loader.load(data, subject);
    }

    public Collection<ServerLoader> getLoaders() {
        return loaders.values();
    }
}
