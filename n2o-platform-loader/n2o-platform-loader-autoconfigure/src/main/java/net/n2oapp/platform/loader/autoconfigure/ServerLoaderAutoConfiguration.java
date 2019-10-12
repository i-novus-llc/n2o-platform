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
    public ServerLoaderRunner jsonLoaderRunner(@Autowired(required = false) List<ServerLoaderConfigurer> configurers) {
        JsonLoaderRunner runner = new JsonLoaderRunner(new ObjectMapper());
        if (configurers != null)
            configurers.forEach(c -> c.configure(runner));
        return runner;
    }
}
