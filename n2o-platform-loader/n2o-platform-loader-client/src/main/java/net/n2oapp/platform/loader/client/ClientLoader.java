package net.n2oapp.platform.loader.client;

import org.springframework.core.io.*;

import java.net.URI;

public interface ClientLoader {

    void load(URI server, String subject, String target, Resource file);
}
