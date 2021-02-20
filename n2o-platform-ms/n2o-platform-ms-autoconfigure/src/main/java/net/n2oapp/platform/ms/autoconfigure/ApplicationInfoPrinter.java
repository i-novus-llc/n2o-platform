package net.n2oapp.platform.ms.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.BuildProperties;

/**
 * Вывод информации о сборке в консоль при старте приложения
 */
public class ApplicationInfoPrinter implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInfoPrinter.class);

    private static final String APP_INFO_TEMPLATE = "\n\nApplication info:"
            + "\n\tgroupId: %s"
            + "\n\tartifactId: %s"
            + "\n\tversion: %s"
            + "\n\tbuilt at: %s\n";

    public ApplicationInfoPrinter(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    private final BuildProperties buildProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (buildProperties != null) {
            LOG.info(String.format(APP_INFO_TEMPLATE,
                    buildProperties.getGroup(),
                    buildProperties.getArtifact(),
                    buildProperties.getVersion(),
                    buildProperties.getTime())
            );
        }
    }
}
