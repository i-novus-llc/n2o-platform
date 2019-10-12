package net.n2oapp.platform.loader.server;

import java.util.Collection;
import java.util.List;

/**
 * Команда для запуска загрузки данных на сервере
 */
public class ServerLoaderCommand {
    private ServerLoader<?> loader;
    private String target;
    private Class<?> type;
    private Class<?> genericType;

    private ServerLoaderCommand(ServerLoader<?> loader, String target, Class<?> type, Class<?> genericType) {
        this.loader = loader;
        this.target = target;
        this.type = type;
        this.genericType = genericType;
    }

    public static <T> ServerLoaderCommand asObject(ServerLoader<T> loader,
                                                   String target,
                                                   Class<T> type) {
        return new ServerLoaderCommand(loader, target, type, null);
    }

    public static <T> ServerLoaderCommand asIterable(ServerLoader<List<T>> loader,
                                                     String target,
                                                     Class<T> type) {
        return new ServerLoaderCommand(loader, target, List.class, type);
    }


    public String getTarget() {
        return target;
    }

    public ServerLoader<?> getLoader() {
        return loader;
    }

    public Class<?> getType() {
        return type;
    }

    public Class<? extends Collection> getIterableType() {
        return (Class<? extends Collection>) type;
    }

    public Class<?> getElementType() {
        return genericType;
    }

    public boolean isIterable() {
        return genericType != null;
    }
}