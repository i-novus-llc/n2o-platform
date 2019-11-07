package net.n2oapp.platform.loader.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки серверного загрузчика
 */
@ConfigurationProperties(prefix = "n2o.loader.server")
public class ServerLoaderProperties {
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
}
