package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.common.util.ClasspathScanner;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

@Configuration
@ConditionalOnClass(Client.class)
public class JaxRsClientAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "cxf.jaxrs.client", name = "address")
    static BeanDefinitionRegistryPostProcessor jaxRsProxyClientsBeanPostProcessor(final ConfigurableEnvironment environment) {
        return new BeanDefinitionRegistryPostProcessor() {
            public void postProcessBeanFactory(ConfigurableListableBeanFactory arg0) throws BeansException {
            }

            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanRegistry) throws BeansException {
                createDynamicBeans(environment, beanRegistry);
            }
        };
    }

    private static void createDynamicBeans(ConfigurableEnvironment environment, BeanDefinitionRegistry beanRegistry) {
        String scanPackages = environment.getProperty("cxf.jaxrs.client.classes-scan-packages");
        String address = environment.getProperty("cxf.jaxrs.client.address");
        String accept = environment.getProperty("cxf.jaxrs.client.accept", "");
        String contentType = environment.getProperty("cxf.jaxrs.client.content-type", "");
        Boolean threadSafe = environment.getProperty("cxf.jaxrs.client.thread-safe", Boolean.class, false);

        final Map<Class<? extends Annotation>, Collection<Class<?>>> classes;
        try {
            classes = ClasspathScanner.findClasses(scanPackages, Path.class);
        } catch (IOException | ClassNotFoundException e) {
            throw new ServiceConstructionException(e);
        }
        classes.get(Path.class).stream()
                .filter(Class::isInterface)
                .forEach(c -> registerJaxRsProxyClient(beanRegistry, address, accept, contentType, threadSafe, c));
    }

    private static void registerJaxRsProxyClient(BeanDefinitionRegistry beanRegistry,
                                                 String address,
                                                 String accept,
                                                 String contentType,
                                                 Boolean threadSafe,
                                                 Class<?> restClass) {
        BeanDefinition definition;
        definition = BeanDefinitionBuilder.genericBeanDefinition(JaxRsClientProxyFactoryBean.class)
                .addPropertyReference("bus", "cxf")
                .addPropertyValue("serviceClass", restClass)
                .addPropertyValue("address", address)
                .addPropertyValue("accept", accept)
                .addPropertyValue("threadSafe", threadSafe)
                .addPropertyValue("contentType", contentType)
                .getBeanDefinition();

        String beanName = restClass.getSimpleName().substring(0, 1).toLowerCase()
                .concat(restClass.getSimpleName().substring(1));
        beanRegistry.registerBeanDefinition(beanName + "JaxRsProxyClient", definition);
    }
}
