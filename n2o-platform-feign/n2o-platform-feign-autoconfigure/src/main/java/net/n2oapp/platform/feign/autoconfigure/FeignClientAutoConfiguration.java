package net.n2oapp.platform.feign.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qualys.feign.jaxrs.EncoderJAXRS3Contract;
import com.qualys.feign.jaxrs.JAXRS3Profile;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import net.n2oapp.platform.jaxrs.autoconfigure.JaxRsCommonAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@AutoConfiguration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignLoadBalancerAutoConfiguration.class)
@AutoConfigureAfter(JaxRsCommonAutoConfiguration.class)
public class FeignClientAutoConfiguration {

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
    public Contract feignContract(@Qualifier("cxfObjectMapper") ObjectMapper mapper) {
        return new EncoderJAXRS3Contract();
    }

    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    @Scope("prototype")
    public JAXRS3Profile feignBuilder(Retryer retryer, feign.Client client) {
        JAXRS3Profile profile = JAXRS3Profile.create();
        profile.retryer(retryer).client(client);
        return profile;
    }
}
