package net.n2oapp.platform.loader.server.repository;

import java.util.List;

@FunctionalInterface
public interface ClientFilter<T> {
    List<T> findAllBySubject(String subject);
}
