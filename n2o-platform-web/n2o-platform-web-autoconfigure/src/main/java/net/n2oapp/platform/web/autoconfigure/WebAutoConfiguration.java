package net.n2oapp.platform.web.autoconfigure;

import net.n2oapp.framework.boot.N2oFrameworkAutoConfiguration;
import net.n2oapp.platform.i18n.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@AutoConfigureBefore(N2oFrameworkAutoConfiguration.class)
@PropertySource("classpath:web.n2o.default.properties")
public class WebAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public PlatformExceptionHandler platformOperationExceptionHandler(@Autowired(required = false) Messages messages) {
        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
        if (messages != null)
            platformExceptionHandler.setMessages(messages);
        return platformExceptionHandler;
    }
}
