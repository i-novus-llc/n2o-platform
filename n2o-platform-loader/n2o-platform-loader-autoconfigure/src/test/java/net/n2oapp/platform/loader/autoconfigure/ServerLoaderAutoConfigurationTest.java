package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.JsonLoaderRunner;
import net.n2oapp.platform.loader.server.ServerLoader;
import net.n2oapp.platform.loader.server.ServerLoaderRunner;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerLoaderAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ServerLoaderAutoConfiguration.class));

    @Test
    public void disabled() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(ServerLoader.class))
                .run((context) -> {
                    assertThat(context).doesNotHaveBean(ServerLoaderRunner.class);
                });
    }

    @Test
    public void loaders() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasSingleBean(JsonLoaderRunner.class);
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    assertThat(runner.getLoaders().size()).isEqualTo(2);
                });
    }

    @Test
    public void settings() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .withPropertyValues(
                        "n2o.loader.server.settings[0].target=load3",
                        "n2o.loader.server.settings[0].create-required=true",
                        "n2o.loader.server.settings[0].update-required=false",
                        "n2o.loader.server.settings[0].delete-required=false",
                        "n2o.loader.server.settings[1].target=load4",
                        "n2o.loader.server.settings[1].create-required=false",
                        "n2o.loader.server.settings[1].update-required=false",
                        "n2o.loader.server.settings[1].delete-required=true")
                .run((context) -> {
                    assertThat(context).hasSingleBean(JsonLoaderRunner.class);
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    assertThat(runner.getLoaders().size()).isEqualTo(2);
                });
    }
}
