package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.client.LoaderRunner;
import net.n2oapp.platform.loader.client.LoaderStarter;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ClientLoaderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ClientLoaderAutoConfiguration.class));

    @Test
    public void startAfterUp() {
        this.contextRunner
                .withPropertyValues("n2o.loader.client.start=UP")
                .run((context) -> {
                    LoaderRunner runner = context.getBean(LoaderRunner.class);
                    assertThat(runner, notNullValue());
                    LoaderStarter starter = context
                            .getBean("startAfterUp", LoaderStarter.class);
                    assertThat(starter, notNullValue());
                    try {
                        context.getBean("startOnDeploy", LoaderStarter.class);
                        fail();
                    } catch (NoSuchBeanDefinitionException ignored) {}
                });
    }

    @Test
    public void startOnDeploy() {
        this.contextRunner
                .withPropertyValues("n2o.loader.client.start=DEPLOY")
                .run((context) -> {
                    LoaderRunner runner = context.getBean(LoaderRunner.class);
                    assertThat(runner, notNullValue());
                    LoaderStarter starter = context
                            .getBean("startOnDeploy", LoaderStarter.class);
                    assertThat(starter, notNullValue());
                    try {
                        context.getBean("startAfterUp", LoaderStarter.class);
                        fail();
                    } catch (NoSuchBeanDefinitionException ignored) {}
                });
    }

    @Test
    public void configurers() {
        this.contextRunner
                .withUserConfiguration(TestClientConfiguration.class)
                .run((context) -> {
                    LoaderRunner runner = context.getBean(LoaderRunner.class);
                    assertThat(runner.getCommands().size(), is(2));
                    assertThat(runner.getLoaders().size(), is(2));
                });
    }

    @Test
    public void commands() {
        this.contextRunner
                .withPropertyValues(
                        "n2o.loader.client.commands[0].server=http://localhost:8080/api",
                        "n2o.loader.client.commands[0].subject=me",
                        "n2o.loader.client.commands[0].target=foo",
                        "n2o.loader.client.commands[0].file=test.json",
                        "n2o.loader.client.commands[1].server=http://localhost:8081/api",
                        "n2o.loader.client.commands[1].subject=me",
                        "n2o.loader.client.commands[1].target=bar",
                        "n2o.loader.client.commands[1].file=test2.json")
                .run((context) -> {
                    LoaderRunner runner = context.getBean(LoaderRunner.class);
                    assertThat(runner.getCommands().size(), is(2));
                });
    }
}
