package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.LoaderReport;
import net.n2oapp.platform.loader.client.LoaderStarter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ClientLoaderHealthIndicator implements HealthIndicator {
    private LoaderStarter starter;
    private boolean healthCheck;

    public ClientLoaderHealthIndicator(LoaderStarter starter, boolean healthCheck) {
        this.starter = starter;
        this.healthCheck = healthCheck;
    }

    @Override
    public Health health() {
        LoaderReport report = starter.getReport();
        if (report == null || !healthCheck)
            return Health.unknown().build();
        else if (!report.isSuccess())
            return Health.down()
                    .withDetail("Loaders failed", report).build();
        else
            return Health.up().build();
    }
}
