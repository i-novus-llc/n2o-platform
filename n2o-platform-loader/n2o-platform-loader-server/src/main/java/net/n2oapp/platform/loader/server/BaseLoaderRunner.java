package net.n2oapp.platform.loader.server;

import org.springframework.aop.TargetClassAware;

import java.io.InputStream;
import java.util.*;

/**
 * Базовый запускатель загрузчиков
 */
public abstract class BaseLoaderRunner implements ServerLoaderRunner, ServerLoaderRestService {
    private List<ServerLoader<?>> loaders;
    private List<ServerLoaderRoute> routes = new ArrayList<>();

    public BaseLoaderRunner(List<ServerLoader<?>> loaders) {
        this.loaders = loaders;
    }

    public ServerLoaderRunner add(ServerLoaderRoute route) {
        routes.add(route);
        return this;
    }

    @Override
    public void run(String subject, String target, InputStream body) {
        ServerLoaderRoute route = findRoute(target);
        Object data = read(body, route);
        ServerLoader<?> loader = findLoader(route);
        execute(subject, data, loader);
    }

    /**
     * Найти загрузчик по цели
     *
     * @param target Цель
     * @return Загрузчик
     */
    protected ServerLoaderRoute findRoute(String target) {
        return routes.stream().filter(l -> l.getTarget().equals(target)).findFirst().orElseThrow();
    }

    /**
     * Прочитать данные
     *
     * @param body    Поток данных
     * @param command Команда
     * @return Данные
     */
    protected abstract Object read(InputStream body, ServerLoaderRoute command);

    protected ServerLoader<?> findLoader(ServerLoaderRoute command) {
        if (command.getLoaderClass() != null) {
            Optional<ServerLoader<?>> loader = loaders.stream()
                    .filter(l -> matches(command.getLoaderClass(), l))
                    .findFirst();
            if (loader.isEmpty())
                throw new IllegalArgumentException("Loader bean " + command.getLoaderClass() + " not found");
            return loader.get();
        } else {
            return loaders.get(0);
        }
    }

    private boolean matches(Class<? extends ServerLoader> loaderClass, ServerLoader<?> loader) {
        return loader.getClass().equals(loaderClass)
                || ((loader instanceof TargetClassAware)
                && (Objects.equals(((TargetClassAware) loader).getTargetClass(), loaderClass)));
    }

    /**
     * Запуск загрузчика
     *
     * @param subject Владелец данных
     * @param data    Данные
     * @param loader  Загрузчик
     */
    @SuppressWarnings("unchecked")
    protected void execute(String subject, Object data, ServerLoader<?> loader) {
        ((ServerLoader<Object>) loader).load(data, subject);
    }

    public List<ServerLoaderRoute> getCommands() {
        return Collections.unmodifiableList(routes);
    }
}
