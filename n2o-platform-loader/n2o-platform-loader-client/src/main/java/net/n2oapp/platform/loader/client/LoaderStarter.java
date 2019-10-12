package net.n2oapp.platform.loader.client;

/**
 * Стартер клиентских загрузок
 */
public class LoaderStarter {
    private ClientLoaderRunner runner;

    public LoaderStarter(ClientLoaderRunner runner) {
        this.runner = runner;
    }

    /**
     * Начать загрузку
     */
    public void start() {
        runner.run();
    }
}
