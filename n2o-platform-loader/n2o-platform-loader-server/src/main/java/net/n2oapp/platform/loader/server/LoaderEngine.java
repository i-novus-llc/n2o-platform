package net.n2oapp.platform.loader.server;

import java.io.InputStream;

public interface LoaderEngine {
    void load(String subject, String target, InputStream body);
}
