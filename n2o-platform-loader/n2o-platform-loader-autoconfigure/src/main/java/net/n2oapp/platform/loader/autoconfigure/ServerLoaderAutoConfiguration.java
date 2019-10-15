package net.n2oapp.platform.loader.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.loader.server.JsonLoaderRunner;
import net.n2oapp.platform.loader.server.ServerLoader;
import net.n2oapp.platform.loader.server.ServerLoaderRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnClass(ServerLoader.class)
public class ServerLoaderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerLoaderRunner jsonLoaderRunner(List<ServerLoader> loaders,
                                               @Autowired(required = false) List<ServerLoaderConfigurer> configurers) {
        return new JsonLoaderRunner(loaders, new ObjectMapper());
    }
}
