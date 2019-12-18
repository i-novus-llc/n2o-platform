package net.n2oapp.platform.loader.server;

/**
 * Настройки серверного загрузчика
 */
public class ServerLoaderSettings<T> implements LoaderDataInfo<T> {

    /**
     * Цель загрузки
     */
    private String target;

    /**
     * Тип данных
     */
    private Class<T> dataType;

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


    public ServerLoaderSettings() {
    }

    public ServerLoaderSettings(String target) {
        this.target = target;
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

    @Override
    public Class<T> getDataType() {
        return dataType;
    }

    public void setDataType(Class<T> dataType) {
        this.dataType = dataType;
    }
}
