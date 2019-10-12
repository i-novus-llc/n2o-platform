package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.JsonLoaderRunner;
import net.n2oapp.platform.loader.server.ServerLoaderEndpoint;
import net.n2oapp.platform.loader.server.LoaderRegister;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServerLoaderAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ServerLoaderAutoConfiguration.class));

    @Test
    public void configurers() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .run((context) -> {
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    assertThat(runner, notNullValue());
                    assertThat(runner.getCommands().size(), is(2));
                });
    }
}
