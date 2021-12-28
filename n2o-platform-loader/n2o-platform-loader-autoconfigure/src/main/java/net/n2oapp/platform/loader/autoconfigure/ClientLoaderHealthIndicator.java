package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.LoaderReport;
import net.n2oapp.platform.loader.client.LoaderStarter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ClientLoaderHealthIndicator implements HealthIndicator {
    private LoaderStarter starter;
    private ClientLoaderProperties properties;

    public ClientLoaderHealthIndicator(LoaderStarter starter, ClientLoaderProperties properties) {
        this.starter = starter;
        this.properties = properties;
    }

    @Override
    public Health health() {
        LoaderReport report = starter.getReport();
        if (report == null)
            return Health.unknown().build();

        else if ((properties == null || properties.isCheckLoaderFails()) && !report.isSuccess())
            return Health.down()
                    .withDetail("Loaders failed", report).build();
        else
            return Health.up().build();
    }
}
