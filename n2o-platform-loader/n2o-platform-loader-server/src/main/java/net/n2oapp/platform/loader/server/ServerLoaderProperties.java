package net.n2oapp.platform.loader.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Настройки серверного загрузчика
 */
@ConfigurationProperties(prefix = "n2o.loader.server")
public class ServerLoaderProperties {
    /**
     * Очередь загрузчиков
     */
    @NestedConfigurationProperty
    private final List<ServerLoaderCommand> commands = new ArrayList<>();

    /**
     * Сохранение данных
     */
    private boolean createRequired = true;

    /**
     * Обновление данных
     */
    private boolean updateRequired = true;

    /**
     * Удаление данных
     */
    private boolean deleteRequired = true;


    public boolean isCreateRequired() {
        return createRequired;
    }

    public void setCreateRequired(boolean createRequired) {
        this.createRequired = createRequired;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public void setUpdateRequired(boolean updateRequired) {
        this.updateRequired = updateRequired;
    }

    public boolean isDeleteRequired() {
        return deleteRequired;
    }

    public void setDeleteRequired(boolean deleteRequired) {
        this.deleteRequired = deleteRequired;
    }

    public List<ServerLoaderCommand> getCommands() {
        return commands;
    }
}
