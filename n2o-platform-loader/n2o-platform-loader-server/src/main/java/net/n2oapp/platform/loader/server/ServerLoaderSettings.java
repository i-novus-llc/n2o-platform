package net.n2oapp.platform.loader.server;

/**
 * Настройки серверного загрузчика
 */
public class ServerLoaderSettings {

    /**
     * Вид данных
     */
    private String target;

    /**
     * Сохранение данных
     */
    private boolean createRequired;

    /**
     * Обновление данных
     */
    private boolean updateRequired;

    /**
     * Удаление данных
     */
    private boolean deleteRequired;


    public ServerLoaderSettings() {
    }

    public ServerLoaderSettings(String target) {
        this.target = target;
        this.createRequired = true;
        this.updateRequired = true;
        this.deleteRequired = true;
    }

    public ServerLoaderSettings(String target, boolean createRequired, boolean updateRequired, boolean deleteRequired) {
        this.target = target;
        this.createRequired = createRequired;
        this.updateRequired = updateRequired;
        this.deleteRequired = deleteRequired;
    }

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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
