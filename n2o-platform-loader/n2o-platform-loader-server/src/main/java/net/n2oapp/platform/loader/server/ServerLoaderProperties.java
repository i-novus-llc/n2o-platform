package net.n2oapp.platform.loader.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Общие настройки серверных загрузчиков
 */
@ConfigurationProperties(prefix = "n2o.loader.server")
public class ServerLoaderProperties {

    /**
     * Очередь загрузчиков
     */
    private final List<ServerLoaderSettings> settings = new ArrayList<>();


    public List<ServerLoaderSettings> getSettings() {
        return settings;
    }
}
