package net.n2oapp.platform.loader.server.repository;

@FunctionalInterface
public interface LoaderMapper<M, E> {
    E map(M model, String subject);
}
