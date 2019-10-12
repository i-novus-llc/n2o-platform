package net.n2oapp.platform.loader.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.loader.server.JsonLoaderEngine;
import net.n2oapp.platform.loader.server.LoaderRegister;
import net.n2oapp.platform.loader.server.ServerLoader;
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
    public LoaderRegister loaderRegister(@Autowired(required = false) List<ServerLoaderConfigurer> configurers) {
        LoaderRegister register = new LoaderRegister();
        if (configurers != null)
            configurers.forEach(c -> c.configure(register));
        return register;
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonLoaderEngine jsonLoaderEngine(LoaderRegister register) {
        return new JsonLoaderEngine(register, new ObjectMapper());
    }
}
