package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.LoaderRunner;

public interface ClientLoaderConfigurer {
    void configure(LoaderRunner runner);
}
