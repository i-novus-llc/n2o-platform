package net.n2oapp.platform.loader.server.repository;

@FunctionalInterface
public interface EntityIdentifier<T, ID> {
    ID identify(T entity);
}
