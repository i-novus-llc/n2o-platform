package net.n2oapp.platform.loader.autoconfigure;

import net.n2oapp.platform.loader.server.LoaderEndpoint;
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
    public void test() {
        this.contextRunner
                .withUserConfiguration(TestServerConfiguration.class)
                .run((context) -> {
                    assertThat(context.getBean(LoaderEndpoint.class), notNullValue());
                    LoaderRegister register = context.getBean(LoaderRegister.class);
                    assertThat(register, notNullValue());
                    assertThat(register.find("load1").getTarget(), is("load1"));
                    assertThat(register.find("load2").getTarget(), is("load2"));
                });
    }
}
