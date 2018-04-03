package net.n2oapp.platform.jaxrs.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.jaxrs.*;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.spring.boot.autoconfigure.CxfProperties;
import org.apache.cxf.validation.BeanValidationInInterceptor;
import org.apache.cxf.validation.BeanValidationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import javax.validation.ValidatorFactory;
import java.util.*;

/**
 * Автоматическая конфигурация REST сервисов
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureBefore(org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration.class)
@EnableConfigurationProperties(JaxRsProperties.class)
public class JaxRsAutoConfiguration {

    private final JaxRsProperties jaxrsProperties;
    private final CxfProperties cxfProperties;

    public JaxRsAutoConfiguration(CxfProperties cxfProperties, JaxRsProperties jaxrsProperties) {
        this.cxfProperties = cxfProperties;
        this.jaxrsProperties = jaxrsProperties;
    }

    @Bean("swagger2Feature")
    @ConditionalOnProperty(prefix = "jaxrs.swagger", name = "enabled", matchIfMissing = true)
    Swagger2Feature swagger2Feature() {
        Swagger2Feature result = new Swagger2Feature();
        result.setTitle(jaxrsProperties.getSwagger().getTitle());
        result.setDescription(jaxrsProperties.getSwagger().getDescription());
        result.setBasePath(cxfProperties.getPath());
        result.setVersion(jaxrsProperties.getSwagger().getVersion());
        result.setSchemes(new String[]{"http", "https"});
        result.setPrettyPrint(true);

        result.setResourcePackage(jaxrsProperties.getSwagger().getResourcePackage());
        return result;
    }

    @Bean("cxfObjectMapper")
    ObjectMapper cxfObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new SpringDataModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new ISO8601DateFormat());
        return mapper;
    }

    @Bean
    JacksonJsonProvider jsonProvider(@Qualifier("cxfObjectMapper") ObjectMapper cxfObjectMapper) {
        return new JacksonJsonProvider(cxfObjectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }

    @Bean
    TypedParamConverter<Date> dateParameterConverter() {
        return new DateISOParameterConverter();
    }

    @Bean
    TypedParamConverter<Sort.Order> sortParameterConverter() {
        return new SortParameterConverter();
    }

    @Bean
    TypedParametersProvider typedParametersProvider(Set<TypedParamConverter<?>> converters) {
        return new TypedParametersProvider(converters);
    }

    @Bean
    @ConditionalOnProperty(prefix="jaxrs", name="log-in", matchIfMissing = true)
    LoggingInInterceptor loggingInInterceptor() {
        return new AnnotatedLoggingInInterceptor();
    }

    @Bean
    @ConditionalOnProperty(prefix="jaxrs", name="log-out", matchIfMissing = true)
    LoggingOutInterceptor loggingOutInterceptor() {
        return new AnnotatedLoggingOutInterceptor();
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
    RestClientExceptionMapper restClientExceptionMapper() {
        return new RestClientExceptionMapper();
    }

//    @Bean
//    LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
//        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
//        localValidatorFactoryBean.setValidationMessageSource(messageSource);
//        return localValidatorFactoryBean;
//    }


//    @Bean(name = Bus.DEFAULT_BUS_ID, destroyMethod = "shutdown")
//    public SpringBus springBus(@Autowired(required = false) Collection<Interceptor<Message>> interceptors) {
//        SpringBus springBus = new SpringBus();
//        if (interceptors != null && !interceptors.isEmpty()) {
//            springBus.getInInterceptors().addAll(interceptors);
//            springBus.getInFaultInterceptors().addAll(interceptors);
//            springBus.getOutInterceptors().addAll(interceptors);
//            springBus.getOutFaultInterceptors().addAll(interceptors);
//        }
//        return springBus;
//    }

}
