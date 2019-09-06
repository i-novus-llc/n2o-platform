package net.n2oapp.platform.jaxrs.autoconfigure;

import brave.Tracing;
import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.jaxrs.*;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.spring.boot.autoconfigure.CxfProperties;
import org.apache.cxf.tracing.brave.jaxrs.BraveFeature;
import org.apache.cxf.validation.BeanValidationInInterceptor;
import org.apache.cxf.validation.BeanValidationProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.ValidatorFactory;

/**
 * Автоматическая конфигурация REST сервисов
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureBefore(org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration.class)
@EnableConfigurationProperties(JaxRsProperties.class)
public class JaxRsServerAutoConfiguration {

    private final JaxRsProperties jaxRsProperties;
    private final CxfProperties cxfProperties;


    public JaxRsServerAutoConfiguration(CxfProperties cxfProperties, JaxRsProperties jaxRsProperties) {
        this.cxfProperties = cxfProperties;
        this.jaxRsProperties = jaxRsProperties;
    }

    @Bean("swagger2Feature")
    @ConditionalOnProperty(prefix = "jaxrs.swagger", name = "enabled", matchIfMissing = true)
    Swagger2Feature swagger2Feature() {
        Swagger2Feature result = new Swagger2Feature();
        result.setTitle(jaxRsProperties.getSwagger().getTitle());
        result.setDescription(jaxRsProperties.getSwagger().getDescription());
        result.setBasePath(cxfProperties.getPath());
        result.setVersion(jaxRsProperties.getSwagger().getVersion());
        result.setSchemes(jaxRsProperties.getSwagger().getSchemes());
        result.setPrettyPrint(true);

        result.setResourcePackage(jaxRsProperties.getSwagger().getResourcePackage());
        return result;
    }

    @Bean
    @ConditionalOnProperty(prefix="jaxrs", name={"log-in", "logging-in.enabled"}, matchIfMissing = true)
    LoggingInInterceptor loggingInInterceptor() {
        AnnotatedLoggingInInterceptor loggingInInterceptor = new AnnotatedLoggingInInterceptor();
        loggingInInterceptor.setLimit(jaxRsProperties.getLoggingIn().getLimit());
        loggingInInterceptor.setInMemThreshold(jaxRsProperties.getLoggingIn().getInMemThreshold());
        loggingInInterceptor.setLogBinary(jaxRsProperties.getLoggingIn().isLogBinary());
        loggingInInterceptor.setLogMultipart(jaxRsProperties.getLoggingIn().isLogMultipart());
        loggingInInterceptor.setPrettyLogging(jaxRsProperties.getLoggingIn().isPrettyLogging());
        return loggingInInterceptor;
    }

    @Bean
    @ConditionalOnProperty(prefix="jaxrs", name={"log-out", "logging-out.enabled"}, matchIfMissing = true)
    LoggingOutInterceptor loggingOutInterceptor() {
        LoggingOutInterceptor loggingOutInterceptor = new AnnotatedLoggingOutInterceptor();
        loggingOutInterceptor.setLimit(jaxRsProperties.getLoggingOut().getLimit());
        loggingOutInterceptor.setInMemThreshold(jaxRsProperties.getLoggingOut().getInMemThreshold());
        loggingOutInterceptor.setLogBinary(jaxRsProperties.getLoggingOut().isLogBinary());
        loggingOutInterceptor.setLogMultipart(jaxRsProperties.getLoggingOut().isLogMultipart());
        loggingOutInterceptor.setPrettyLogging(jaxRsProperties.getLoggingOut().isPrettyLogging());
        return loggingOutInterceptor;
    }

    @Bean
    @ConditionalOnProperty(prefix="jaxrs", name="jsr303", matchIfMissing = true)
    BeanValidationInInterceptor beanValidationInInterceptor(ValidatorFactory validatorFactory) {
        JAXRSBeanValidationInInterceptor validationInInterceptor = new JAXRSBeanValidationInInterceptor();
        BeanValidationProvider validationProvider = new BeanValidationProvider(validatorFactory);
        validationInInterceptor.setProvider(validationProvider);
        return validationInInterceptor;
    }

    @Bean
    ViolationRestExceptionMapper violationExceptionMapper() {
        return new ViolationRestExceptionMapper();
    }

    @Bean
    RestServerExceptionMapper restServerExceptionMapper() {
        return new RestServerExceptionMapper();
    }

    @Bean
    @ConditionalOnClass(Messages.class)
    MessageExceptionMapper messageExceptionMapper(Messages messages) {
        return new MessageExceptionMapper(messages);
    }

    @Bean
    @ConditionalOnProperty(value = {"spring.sleuth.enabled"})
    BraveFeature braveFeature(Tracing brave) {
        return new BraveFeature(brave);
    }
}
