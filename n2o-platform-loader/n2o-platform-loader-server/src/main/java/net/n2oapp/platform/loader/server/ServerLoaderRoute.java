package net.n2oapp.platform.loader.server;

import java.util.Collection;
import java.util.List;

/**
 * Информация для запуска загрузчика на сервере
 */
public class ServerLoaderRoute {
    private String target;
    private Class<?> type;
    private Class<?> elementType;
    private Class<? extends ServerLoader> loaderClass;

    public static <T> ServerLoaderRoute asObject(String target, Class<T> type,
                                                 Class<? extends ServerLoader> loaderType) {
        return new ServerLoaderRoute(target, type, null, loaderType);
    }

    public static <T> ServerLoaderRoute asIterable(String target,
                                                   Class<T> type,
                                                   Class<? extends ServerLoader> loaderType) {
        return new ServerLoaderRoute(target, List.class, type, loaderType);
    }

    public ServerLoaderRoute(String target, Class<?> type, Class<?> elementType,
                              Class<? extends ServerLoader> loaderClass) {
        this.target = target;
        this.type = type;
        this.elementType = elementType;
        this.loaderClass = loaderClass;
    }

    public ServerLoaderRoute() {
    }

    public String getTarget() {
        return target;
    }

    public Class<?> getType() {
        return !isIterable()? type : null;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Collection> getIterableType() {
        return (isIterable() ? (Class<? extends Collection>) type : null);
    }

    public Class<?> getElementType() {
        return elementType;
    }

    public boolean isIterable() {
        return elementType != null;
    }

    public Class<? extends ServerLoader> getLoaderClass() {
        return loaderClass;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setElementType(Class<?> elementType) {
        this.elementType = elementType;
    }

    public void setLoaderClass(Class<? extends ServerLoader> loaderClass) {
        this.loaderClass = loaderClass;
    }
}