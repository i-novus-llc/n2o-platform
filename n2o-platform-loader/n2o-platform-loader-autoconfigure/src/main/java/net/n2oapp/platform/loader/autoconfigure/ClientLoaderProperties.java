package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.LoaderCommand;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "n2o.loader.client")
public class ClientLoaderProperties {
    /**
     * Очередь загрузчиков
     */
    @NestedConfigurationProperty
    private final List<LoaderCommand> commands = new ArrayList<>();
    /**
     * Момент запуска загрузчиков
     */
    private StartingTime start = StartingTime.UP;
    /**
     * Прекращать загрузку при первой же ошибке
     */
    private boolean failFast = true;
    /**
     * Таймаут каждого загрузчика (сек)
     */
    private int timeout = 60;
    /**
     * Количество повторных попыток запуска при неудачах
     */
    private int retries = 0;
    /**
     *  Интервал между повторными попытками (сек)
     */
    private int retriesInterval = 60;


    enum StartingTime {
        DEPLOY,
        UP
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public StartingTime getStart() {
        return start;
    }

    public void setStart(StartingTime start) {
        this.start = start;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getRetriesInterval() {
        return retriesInterval;
    }

    public void setRetriesInterval(int retriesInterval) {
        this.retriesInterval = retriesInterval;
    }

    public List<LoaderCommand> getCommands() {
        return commands;
    }
}
