package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.ServerLoaderRoute;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Настройки серверного загрузчика
 */
@ConfigurationProperties(prefix = "n2o.loader.server")
public class ServerLoaderProperties {
    private final List<ServerLoaderRoute> routes = new ArrayList<>();

    public List<ServerLoaderRoute> getRoutes() {
        return routes;
    }
}
