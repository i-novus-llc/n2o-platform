package net.n2oapp.platform.loader.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.loader.server.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnClass(ServerLoader.class)
@EnableConfigurationProperties(ServerLoaderProperties.class)
public class ServerLoaderAutoConfiguration {

    @Autowired
    private ServerLoaderProperties serverLoaderProperties;
    private Map<String, ServerLoaderSettings> settingsByTarget;

    @PostConstruct
    public void init() {
        settingsByTarget = (serverLoaderProperties == null) ? new HashMap<>() :
                serverLoaderProperties.getSettings().stream()
                        .collect(Collectors.toMap(ServerLoaderSettings::getTarget, s -> s));
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerLoaderRunner jsonLoaderRunner(List<ServerLoader> loaders,
                                               @Autowired(required = false) List<ServerLoaderConfigurer> configurers) {
        JsonLoaderRunner runner = new JsonLoaderRunner(loaders, new ObjectMapper());
        if (configurers != null)
            configurers.forEach(c -> c.configure(runner));
        return runner;
    }

    @Component
    class ServerLoaderPostProcessorImpl implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof BaseServerLoader) {
                ServerLoaderSettings serverLoaderSettings = settingsByTarget.get(((BaseServerLoader) bean).getTarget());
                if (serverLoaderSettings != null) {
                    ((BaseServerLoader) bean).setCreateRequired(serverLoaderSettings.isCreateRequired());
                    ((BaseServerLoader) bean).setUpdateRequired(serverLoaderSettings.isUpdateRequired());
                    ((BaseServerLoader) bean).setDeleteRequired(serverLoaderSettings.isDeleteRequired());
                }
            }
            return bean;
        }
    }
}
