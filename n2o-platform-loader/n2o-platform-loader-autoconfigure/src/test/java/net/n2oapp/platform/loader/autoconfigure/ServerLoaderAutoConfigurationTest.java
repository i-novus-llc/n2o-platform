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
    public void routes() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .withPropertyValues(
                        "n2o.loader.server.routes[0].target=route1",
                        "n2o.loader.server.routes[0].type=java.lang.Object",
                        "n2o.loader.server.routes[0].loader-class=net.n2oapp.platform.loader.autoconfigure.TestServerConfiguration.MyLoader1",
                        "n2o.loader.server.routes[1].target=route2",
                        "n2o.loader.server.routes[1].element-type=java.lang.Object",
                        "n2o.loader.server.routes[1].loader-class=net.n2oapp.platform.loader.autoconfigure.TestServerConfiguration.MyLoader2"
                        )
                .run((context) -> {
                    assertThat(context).hasSingleBean(JsonLoaderRunner.class);
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    assertThat(runner.getCommands().size()).isEqualTo(2);
                });
    }

    @Test
    public void configurers() {
        this.contextRunner
                .withUserConfiguration(
                        TestServerConfiguration.class,
                        TestServerConfiguration.RoutesConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasSingleBean(JsonLoaderRunner.class);
                    JsonLoaderRunner runner = context.getBean(JsonLoaderRunner.class);
                    assertThat(runner.getCommands().size()).isEqualTo(2);
                });
    }
}
