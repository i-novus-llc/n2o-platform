package net.n2oapp.platform.loader.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.loader.server.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AutoConfiguration
@ConditionalOnClass(ServerLoader.class)
@EnableConfigurationProperties(ServerLoaderProperties.class)
public class ServerLoaderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerLoaderRunner jsonLoaderRunner(List<ServerLoader> loaders,
                                               @Autowired(required = false) List<ServerLoaderConfigurer> configurers) {
        JsonLoaderRunner runner = new JsonLoaderRunner(loaders, new ObjectMapper());
        if (configurers != null)
            configurers.forEach(c -> c.configure(runner));
        return runner;
    }

    @Configuration
    static class ServerLoaderPostProcessorImpl implements BeanPostProcessor {

        @Autowired
        private ServerLoaderProperties serverLoaderProperties;
        private Map<String, ServerLoaderSettings> settingsByTarget;

        @PostConstruct
        public void init() {
            settingsByTarget = (serverLoaderProperties == null) ? new HashMap<>() :
                    serverLoaderProperties.getSettings().stream()
                            .collect(Collectors.toMap(ServerLoaderSettings::getTarget, s -> s));
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof ServerLoaderSettings) {
                ServerLoaderSettings loader = (ServerLoaderSettings) bean;
                ServerLoaderSettings settings = settingsByTarget.get(loader.getTarget());
                if (settings != null) {
                    loader.setCreateRequired(settings.isCreateRequired());
                    loader.setUpdateRequired(settings.isUpdateRequired());
                    loader.setDeleteRequired(settings.isDeleteRequired());
                }
            }
            return bean;
        }
    }
}
