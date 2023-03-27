package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.ClientLoader;
import net.n2oapp.platform.loader.client.ClientLoaderRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ClientLoaderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ClientLoaderAutoConfiguration.class));

    @Test
    void disabled() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(ClientLoader.class))
                .run((context) -> {
                    assertThat(context).doesNotHaveBean(ClientLoaderRunner.class);
                });
    }

    @Test
    void startAfterUp() {
        this.contextRunner
                .withPropertyValues("n2o.loader.client.start=UP")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderRunner.class);
                    assertThat(context).hasBean("startAfterUp");
                    assertThat(context).doesNotHaveBean("startOnDeploy");
                    assertThat(context).doesNotHaveBean("startManual");
                    assertThat(context).doesNotHaveBean("startDelayed");
                });
    }

    @Test
    void startOnDeploy() {
        this.contextRunner
                .withPropertyValues("n2o.loader.client.start=DEPLOY")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderRunner.class);
                    assertThat(context).hasBean("startOnDeploy");
                    assertThat(context).doesNotHaveBean("startAfterUp");
                    assertThat(context).doesNotHaveBean("startManual");
                    assertThat(context).doesNotHaveBean("startDelayed");
                });
    }

    @Test
    void startManual() {
        this.contextRunner
                .withPropertyValues("n2o.loader.client.start=MANUAL")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderRunner.class);
                    assertThat(context).hasBean("startManual");
                    assertThat(context).doesNotHaveBean("startOnDeploy");
                    assertThat(context).doesNotHaveBean("startAfterUp");
                    assertThat(context).doesNotHaveBean("startDelayed");
                });
    }

    @Test
    void startDelayed() {
        this.contextRunner
                .withPropertyValues("n2o.loader.client.start=DELAYED")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderRunner.class);
                    assertThat(context).hasBean("startDelayed");
                    assertThat(context).doesNotHaveBean("startOnDeploy");
                    assertThat(context).doesNotHaveBean("startAfterUp");
                    assertThat(context).doesNotHaveBean("startManual");
                });
    }

    @Test
    void configurers() {
        this.contextRunner
                .withUserConfiguration(TestClientConfiguration.class)
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderRunner.class);
                    ClientLoaderRunner runner = context.getBean(ClientLoaderRunner.class);
                    assertThat(runner.getCommands().size()).isEqualTo(2);
                    assertThat(runner.getLoaders().size()).isEqualTo(2);
                });
    }

    @Test
    void commands() {
        this.contextRunner
                .withPropertyValues(
                        "n2o.loader.client.commands[0].server=http://localhost:8080/api",
                        "n2o.loader.client.commands[0].auth.client-id=test_client",
                        "n2o.loader.client.commands[0].auth.client-secret=test_secret",
                        "n2o.loader.client.commands[0].auth.token-uri=token_url",
                        "n2o.loader.client.commands[0].subject=me",
                        "n2o.loader.client.commands[0].target=foo",
                        "n2o.loader.client.commands[0].file=test.json",
                        "n2o.loader.client.commands[1].server=http://localhost:8081/api",
                        "n2o.loader.client.commands[1].auth.username=test_user",
                        "n2o.loader.client.commands[1].auth.password=test_password",
                        "n2o.loader.client.commands[1].subject=me",
                        "n2o.loader.client.commands[1].target=bar",
                        "n2o.loader.client.commands[1].file=test2.json")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderRunner.class);
                    ClientLoaderRunner runner = context.getBean(ClientLoaderRunner.class);
                    assertThat(runner.getCommands().size()).isEqualTo(2);
                    assertThat(runner.getCommands().get(0).getAuth().getClientId()).isEqualTo("test_client");
                    assertThat(runner.getCommands().get(0).getAuth().getClientSecret()).isEqualTo("test_secret");
                    assertThat(runner.getCommands().get(0).getAuth().getTokenUri()).isEqualTo("token_url");
                    assertThat(runner.getCommands().get(1).getAuth().getUsername()).isEqualTo("test_user");
                    assertThat(runner.getCommands().get(1).getAuth().getPassword()).isEqualTo("test_password");
                });
    }

    @Test
    void health() {
        this.contextRunner
                .withPropertyValues(
                        "n2o.loader.client.health-check=false")
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderHealthIndicator.class);
                    ClientLoaderHealthIndicator healthIndicator = context.getBean(ClientLoaderHealthIndicator.class);
                    assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UNKNOWN);
                });
        this.contextRunner
                .run((context) -> {
                    assertThat(context).hasSingleBean(ClientLoaderHealthIndicator.class);
                    ClientLoaderHealthIndicator healthIndicator = context.getBean(ClientLoaderHealthIndicator.class);
                    assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
                });
    }
}
