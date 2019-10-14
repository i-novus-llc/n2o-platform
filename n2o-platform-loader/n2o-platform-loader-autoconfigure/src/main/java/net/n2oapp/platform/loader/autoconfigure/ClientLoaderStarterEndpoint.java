package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.ClientLoaderRunner;
import net.n2oapp.platform.loader.client.LoaderReport;
import net.n2oapp.platform.loader.client.LoaderStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.annotation.Lazy;

@Endpoint(id = "loaders")
public class ClientLoaderStarterEndpoint {
    @Lazy
    @Autowired
    private LoaderStarter starter;

    @WriteOperation
    public LoaderReport start() {
        starter.start();
        return starter.getReport();
    }

    @ReadOperation
    public LoaderReport status() {
        return starter.getReport();
    }

    public void setStarter(LoaderStarter starter) {
        this.starter = starter;
    }
}
