package net.n2oapp.platform.web.autoconfigure;

import net.n2oapp.framework.api.data.OperationExceptionHandler;
import net.n2oapp.framework.api.data.QueryExceptionHandler;
import net.n2oapp.framework.boot.N2oAutoConfiguration;
import net.n2oapp.platform.i18n.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(N2oAutoConfiguration.class)
public class WebAutoConfiguration {

    @Bean
    public PlatformExceptionHandler platformOperationExceptionHandler(@Autowired(required = false) Messages messages) {
        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
        if (messages != null)
            platformExceptionHandler.setMessages(messages);
        return platformExceptionHandler;
    }

//    @Bean
//    public QueryExceptionHandler platformQueryExceptionHandler(@Autowired(required = false) Messages messages) {
//        PlatformExceptionHandler platformExceptionHandler = new PlatformExceptionHandler();
//        if (messages != null)
//            platformExceptionHandler.setMessages(messages);
//        return platformExceptionHandler;
//    }
}
