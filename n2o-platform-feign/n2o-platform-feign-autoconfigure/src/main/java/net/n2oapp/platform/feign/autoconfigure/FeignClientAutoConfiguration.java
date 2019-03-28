package net.n2oapp.platform.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qualys.feign.jaxrs.JAXRS2Profile;
import feign.Contract;
import feign.Feign;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import net.n2oapp.platform.jaxrs.MapperConfigurer;
import net.n2oapp.platform.jaxrs.RestObjectMapper;
import net.n2oapp.platform.jaxrs.autoconfigure.JaxRsCommonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignRibbonClientAutoConfiguration.class)
@AutoConfigureAfter(JaxRsCommonAutoConfiguration.class)
public class FeignClientAutoConfiguration {

    private List<MapperConfigurer> mapperConfigurers;

    public FeignClientAutoConfiguration(@Autowired(required = false) List<MapperConfigurer> mapperConfigurers) {
        this.mapperConfigurers = mapperConfigurers;
    }

    @ConditionalOnMissingBean
    @Bean("cxfObjectMapper")// По контракту мы должны использовать серверный маппер
    public ObjectMapper cxfObjectMapper() {
        return new RestObjectMapper(mapperConfigurers);
    }

    @Bean
    public Decoder feignDecoder(@Qualifier("cxfObjectMapper") ObjectMapper cxfObjectMapper) {
        return new JacksonDecoder(cxfObjectMapper);
    }

    @Bean
    public Encoder feignEncoder(@Qualifier("cxfObjectMapper") ObjectMapper cxfObjectMapper) {
        return new JacksonEncoder(cxfObjectMapper);
    }

    @Bean
    public ErrorDecoder feignErrorDecoder(@Qualifier("cxfObjectMapper") ObjectMapper cxfObjectMapper) {
        return new FeignErrorDecoder(cxfObjectMapper);
    }

    @Bean
    public Contract feignContract() {
        return new JAXRSContract();
    }

    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder(Retryer retryer, feign.Client client) {
        return JAXRS2Profile.create().client(client).retryer(retryer);
    }
}
