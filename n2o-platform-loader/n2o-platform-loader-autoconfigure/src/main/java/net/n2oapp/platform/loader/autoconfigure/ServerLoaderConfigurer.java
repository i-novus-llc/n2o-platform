package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.LoaderRegister;

public interface ServerLoaderConfigurer {
    void configure(LoaderRegister register);
}
