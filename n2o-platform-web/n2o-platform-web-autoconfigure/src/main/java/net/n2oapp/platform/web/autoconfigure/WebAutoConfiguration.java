package net.n2oapp.platform.web.autoconfigure;

import net.n2oapp.platform.i18n.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAutoConfiguration {

    @Bean
    public PlatformExceptionHandler exceptionHandler(@Autowired(required = false) Messages messages) {
        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
        if (messages != null)
            platformExceptionHandler.setMessages(messages);
        return platformExceptionHandler;
    }

}
