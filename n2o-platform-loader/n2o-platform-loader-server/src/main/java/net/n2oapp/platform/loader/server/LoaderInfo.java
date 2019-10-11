package net.n2oapp.platform.loader.server;

import java.util.Collection;
import java.util.List;

public class LoaderInfo {
    private ServerLoader<?> loader;
    private String target;
    private Class<?> type;
    private Class<?> genericType;

    private LoaderInfo(ServerLoader<?> loader, String target, Class<?> type, Class<?> genericType) {
        this.loader = loader;
        this.target = target;
        this.type = type;
        this.genericType = genericType;
    }

    public static <T> LoaderInfo asObject(ServerLoader<T> loader,
                                          String target,
                                          Class<T> type) {
        return new LoaderInfo(loader, target, type, null);
    }

    public static <T> LoaderInfo asIterable(ServerLoader<List<T>> loader,
                                            String purpose,
                                            Class<T> type) {
        return new LoaderInfo(loader, purpose, List.class, type);
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