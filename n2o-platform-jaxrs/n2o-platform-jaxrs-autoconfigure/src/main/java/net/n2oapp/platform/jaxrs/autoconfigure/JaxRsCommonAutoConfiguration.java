package net.n2oapp.platform.jaxrs.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.n2oapp.platform.jaxrs.*;
import org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author RMakhmutov
 * @since 14.01.2019
 */
@Configuration
@AutoConfigureBefore({CxfAutoConfiguration.class, JaxRsServerAutoConfiguration.class, JaxRsClientAutoConfiguration.class})
public class JaxRsCommonAutoConfiguration {
    private List<MapperConfigurer> mapperConfigurers;

    public JaxRsCommonAutoConfiguration(@Autowired(required = false)List<MapperConfigurer> mapperConfigurers) {
        this.mapperConfigurers = mapperConfigurers;
    }

    @Bean("cxfObjectMapper")
    ObjectMapper cxfObjectMapper() {
        return new RestObjectMapper(mapperConfigurers);
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
    TypedParamConverter<ZonedDateTime> zonedDateTimeTypedParamConverter() {
        return new ZonedDateTimeParamConverter();
    }

    @Bean
    TypedParametersProvider typedParametersProvider(Set<TypedParamConverter<?>> converters) {
        return new TypedParametersProvider(converters);
    }
}
