package net.n2oapp.platform.loader.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Стартер клиентских загрузок
 */
public class LoaderStarter {
    private static final Logger logger = LoggerFactory.getLogger(LoaderStarter.class);
    private ClientLoaderRunner runner;
    private LoaderReport report;
    private Integer retries;
    private Integer retriesInterval;

    public LoaderStarter(ClientLoaderRunner runner, Integer retries, Integer retriesInterval) {
        this.runner = runner;
        this.retries = retries;
        this.retriesInterval = retriesInterval;
        this.report = new LoaderReport();
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
            if (logger.isErrorEnabled()) {
                logger.error(String.format("Loading failed: %d fails, %d success, %d aborted.",
                        report.getFails().size(),
                        report.getSuccess().size(),
                        report.getAborted().size()));
            }
            report.getFails().forEach(fail ->
                    logger.debug(String.format("Loading %s failed by %s",
                            fail.getCommand().getTarget(), fail.getException().getMessage()),
                            fail.getException()));
            if (retries > 0) {
                logger.info("Failed loaders will start again in {} seconds. {} tries remaining..",
                        retriesInterval, retries);
                runner.setCommands(report.getFails().stream()
                        .map(LoaderReport.Fail::getCommand)
                        .collect(Collectors.toList()));
                retries--;
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                service.schedule(this::start, retriesInterval, TimeUnit.SECONDS);
                service.shutdown();
            }
        }

    }

    /**
     * Отчёт о последнем запуске загрузчиков
     */
    public LoaderReport getReport() {
        return report;
    }
}
