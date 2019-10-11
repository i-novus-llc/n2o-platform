package net.n2oapp.platform.loader.server;

public interface ServerLoader<T> {
    void load(T data, String subject);
}
