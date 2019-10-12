package net.n2oapp.platform.loader.client;

public class LoaderStarter {
    private LoaderRunner runner;

    public LoaderStarter(LoaderRunner runner) {
        this.runner = runner;
    }

    public void start() {
        runner.run();
    }
}
