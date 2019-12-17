package net.n2oapp.platform.loader.server;

@FunctionalInterface
public interface EntityIdentifier<T, ID> {
    ID identify(T entity);
}
