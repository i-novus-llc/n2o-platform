package net.n2oapp.platform.security;

import net.n2oapp.platform.security.autoconfigure.SecurityAutoConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Тесты автоконфигурации {@link SecurityAutoConfiguration}
 */
public class SecurityAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class));

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class));

    /**
     * Проверка, что с настройкой n2o.platform.security.key-set-uri создадутся бины для jwt
     */
    @Test
    public void test() {
        this.contextRunner
                .withPropertyValues("n2o.platform.security.key-set-uri=http:/example.com/certs")
                .run((context) -> {
                    assertThat(context.getBean(TokenStore.class), notNullValue());
                    assertThat(context.getBean(ResourceServerTokenServices.class), notNullValue());
                });
    }

    /**
     * Проверка, что без настройки n2o.platform.security.key-set-uri упадет ошибка
     */
    @Test
    public void testException() {
        this.contextRunner
                .run((context) -> {
                    assertThat(context.getStartupFailure(), instanceOf(UnsatisfiedDependencyException.class));
                    assertThat(((UnsatisfiedDependencyException)context.getStartupFailure()).getBeanName(), is("tokenServices"));
                });
    }
}
