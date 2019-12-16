package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.BaseServerLoader;
import net.n2oapp.platform.loader.server.JsonLoaderRunner;
import net.n2oapp.platform.loader.server.ServerLoader;
import net.n2oapp.platform.loader.server.ServerLoaderRunner;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

                    Map<String, boolean[]> expectedResult = Map.of(
                            "load1", new boolean[]{true, false, false},
                            "load2", new boolean[]{false, false, true}
                    );

                    BaseServerLoader serverLoader = (BaseServerLoader) loaders.get(0);
                    assertThat(expectedResult.containsKey(serverLoader.getTarget())).isEqualTo(true);
                    boolean[] settings = expectedResult.get(serverLoader.getTarget());
                    assertThat(serverLoader.isCreateRequired()).isEqualTo(settings[0]);
                    assertThat(serverLoader.isUpdateRequired()).isEqualTo(settings[1]);
                    assertThat(serverLoader.isDeleteRequired()).isEqualTo(settings[2]);

                    serverLoader = (BaseServerLoader) loaders.get(1);
                    assertThat(expectedResult.containsKey(serverLoader.getTarget())).isEqualTo(true);
                    settings = expectedResult.get(serverLoader.getTarget());
                    assertThat(serverLoader.isCreateRequired()).isEqualTo(settings[0]);
                    assertThat(serverLoader.isUpdateRequired()).isEqualTo(settings[1]);
                    assertThat(serverLoader.isDeleteRequired()).isEqualTo(settings[2]);
                });
    }
}
