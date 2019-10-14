package net.n2oapp.platform.loader.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Стартер клиентских загрузок
 */
public class LoaderStarter {
    private static final Logger logger = LoggerFactory.getLogger(LoaderStarter.class);
    private ClientLoaderRunner runner;
    private LoaderReport report;

    public LoaderStarter(ClientLoaderRunner runner) {
        this.runner = runner;
    }

    /**
     * Начать загрузку
     */
    public synchronized void start() {
        logger.info("Loading started...");
        report = runner.run();
        if (report.isSuccess())
            logger.info("Loading success finished!");
        else {
            logger.error("Loading failed: {0} fails, {1} success, {2} aborted.",
                    report.getFails().size(),
                    report.getSuccess().size(),
                    report.getAborted().size());
            report.getFails().forEach(fail ->
                    logger.debug("Loading {0} failed by {1}",
                            fail.getCommand().getTarget(), fail.getException().getMessage(),
                            fail.getException()));
        }

    }

    /**
     * Отчёт о последнем запуске загрузчиков
     */
    public LoaderReport getReport() {
        return report;
    }
}
