package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.LoaderReport;
import net.n2oapp.platform.loader.client.LoaderStarter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ClientLoaderHealthIndicator implements HealthIndicator {
    private LoaderStarter starter;

    public ClientLoaderHealthIndicator(LoaderStarter starter) {
        this.starter = starter;
    }

    @Override
    public Health health() {
        LoaderReport report = starter.getReport();
        if (!report.isSuccess()) {
            return Health.down()
                    .withDetail("Loaders failed", report.getFails()).build();
        }
        return Health.up().build();
    }
}
