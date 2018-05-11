package net.n2oapp.platform.jaxrs.autoconfigure;

import org.apache.cxf.common.util.ClasspathScanner;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Поиск и регистрация JaxRS прокси клиентов в качестве Spring бинов.
 * @see EnableJaxRsProxyClient
 */
public class JaxRsProxyClientRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;
    private List<Class<?>> classes = Collections.emptyList();
    private String scanPackages;
    private String address;
    private String accept;
    private String contentType;
    private Boolean threadSafe;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        overrideProperties(importingClassMetadata);
        if (classes.isEmpty())
            classes = findClasses();
        classes.forEach(c -> registerJaxRsProxyClient(registry, c));
    }

    private void overrideProperties(AnnotationMetadata importingClassMetadata) {
        Class<EnableJaxRsProxyClient> annoType = EnableJaxRsProxyClient.class;
        Map<String, Object> annotationAttributes = importingClassMetadata
                .getAnnotationAttributes(annoType.getName(), false);
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(annotationAttributes);
        if (attributes == null)
            return;
        String address = attributes.getString("address");
        if (address != null && !address.isEmpty())
            this.address = environment.resolvePlaceholders(address);
        Class<?>[] classes = attributes.getClassArray("classes");
        if (classes == null || classes.length == 0)
            classes = attributes.getClassArray("value");
        if (classes != null && classes.length > 0)
            this.classes = Arrays.asList(classes);
        String[] scanPackages = attributes.getStringArray("scanPackages");
        if (scanPackages != null && scanPackages.length > 0)
            this.scanPackages = Stream.of(scanPackages).reduce((a,b) -> a + "," + b).get();
    }

    private List<Class<?>> findClasses() {
        if (scanPackages == null)
            throw new IllegalArgumentException("You need to set property [cxf.jaxrs.client.classes-scan-packages]");
        final Map<Class<? extends Annotation>, Collection<Class<?>>> classes;
        try {
            classes = ClasspathScanner.findClasses(scanPackages, Path.class);
        } catch (IOException | ClassNotFoundException e) {
            throw new ServiceConstructionException(e);
        }
        return classes.get(Path.class).stream()
                .filter(Class::isInterface).collect(Collectors.toList());
    }

    private void registerJaxRsProxyClient(BeanDefinitionRegistry beanRegistry,
                                          Class<?> restClass) {
        BeanDefinition definition;
        definition = BeanDefinitionBuilder.genericBeanDefinition(JaxRsProxyClientFactoryBean.class)
                .addPropertyReference("bus", "cxf")
                .addPropertyValue("serviceClass", restClass)
                .addPropertyValue("address", address)
                .addPropertyValue("accept", accept)
                .addPropertyValue("threadSafe", threadSafe)
                .addPropertyValue("contentType", contentType)
                .getBeanDefinition();

        String beanName = generateProxyBeanName(restClass);
        beanRegistry.registerBeanDefinition(beanName, definition);
    }

    private String generateProxyBeanName(Class<?> restClass) {
        return restClass.getSimpleName().substring(0, 1).toLowerCase()
                .concat(restClass.getSimpleName().substring(1)) + "JaxRsProxyClient";
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.scanPackages = environment.getProperty("cxf.jaxrs.client.classes-scan-packages");
        this.address = environment.getProperty("cxf.jaxrs.client.address");
        this.accept = environment.getProperty("cxf.jaxrs.client.accept", "");
        this.contentType = environment.getProperty("cxf.jaxrs.client.content-type", "");
        this.threadSafe = environment.getProperty("cxf.jaxrs.client.thread-safe", Boolean.class, false);
    }
}
