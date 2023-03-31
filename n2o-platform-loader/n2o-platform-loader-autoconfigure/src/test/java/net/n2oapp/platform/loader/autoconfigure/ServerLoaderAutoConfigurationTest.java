package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.JsonLoaderRunner;
import net.n2oapp.platform.loader.server.ServerLoader;
import net.n2oapp.platform.loader.server.ServerLoaderRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServerLoaderAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ServerLoaderAutoConfiguration.class));

    @Test
    void disabled() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(ServerLoader.class))
                .run((context) -> {
                    assertThat(context).doesNotHaveBean(ServerLoaderRunner.class);
                });
    }

    @Test
    void loaders() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasSingleBean(JsonLoaderRunner.class);
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    assertThat(runner.getLoaders().size()).isEqualTo(2);
                });
    }

    @Test
    void settings() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .withPropertyValues(
                        "n2o.loader.server.settings[0].target=load1",
                        "n2o.loader.server.settings[0].create-required=true",
                        "n2o.loader.server.settings[0].update-required=false",
                        "n2o.loader.server.settings[0].delete-required=false",
                        "n2o.loader.server.settings[1].target=load2",
                        "n2o.loader.server.settings[1].create-required=false",
                        "n2o.loader.server.settings[1].update-required=false",
                        "n2o.loader.server.settings[1].delete-required=true")
                .run((context) -> {
                    assertThat(context).hasSingleBean(JsonLoaderRunner.class);
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    List<ServerLoader> loaders = new ArrayList<>(runner.getLoaders());
                    assertThat(loaders.size()).isEqualTo(2);

                    TestServerConfiguration.MyLoader1 loader1 = context.getBean(TestServerConfiguration.MyLoader1.class);
                    assertThat(loader1.isCreateRequired()).isEqualTo(true);
                    assertThat(loader1.isUpdateRequired()).isEqualTo(false);
                    assertThat(loader1.isDeleteRequired()).isEqualTo(false);

                    TestServerConfiguration.MyLoader2 loader2 = context.getBean(TestServerConfiguration.MyLoader2.class);
                    assertThat(loader2.isCreateRequired()).isEqualTo(false);
                    assertThat(loader2.isUpdateRequired()).isEqualTo(false);
                    assertThat(loader2.isDeleteRequired()).isEqualTo(true);
                });
    }
}
