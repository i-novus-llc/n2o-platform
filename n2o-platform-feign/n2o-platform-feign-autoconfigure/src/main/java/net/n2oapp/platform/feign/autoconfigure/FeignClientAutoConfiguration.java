package net.n2oapp.platform.feign.autoconfigure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qualys.feign.jaxrs.EncoderContext;
import feign.*;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import net.n2oapp.platform.jaxrs.autoconfigure.JaxRsCommonAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.OAuth2ClientContext;

import javax.ws.rs.QueryParam;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;

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
        return new JAXRSContract() {
            @Override
            protected void registerParamAnnotations() {
                super.registerParamAnnotations();
                registerParameterAnnotation(QueryParam.class, (param, data, paramIndex) -> {
                    final String name = param.value();
                    checkState(emptyToNull(name) != null, "QueryParam.value() was empty on parameter %s", paramIndex);
                    final String query = String.format("{%s}", name);
                    data.template().query(name, query);
                    nameParam(data, name, paramIndex);
                    if (Map.class.isAssignableFrom(data.method().getParameters()[paramIndex].getType())) {
                        if (data.indexToExpander() == null)
                            data.indexToExpander(new HashMap<>());
                        data.indexToExpander().put(paramIndex, value -> {
                            try {
                                return mapper.writeValueAsString(value);
                            } catch (JsonProcessingException e) {
                                throw new IllegalStateException("Can't expand parameter value via jackson mapper. Value: " + value);
                            }
                        });
                    }
                });
            }
        };
    }

    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Configuration
    @ConditionalOnClass(OAuth2ClientContext.class)
    public static class FeignJwtHeaderInterceptorConfig {
        @Bean
        @ConditionalOnMissingBean
        public FeignJwtHeaderInterceptor feignJwtHeaderInterceptor(OAuth2ClientContext oAuth2ClientContext) {
            return new FeignJwtHeaderInterceptor(oAuth2ClientContext);
        }
    }

    @Bean
    @Scope("prototype")
    public JAXRS2ProfileExtended feignBuilder(Retryer retryer, feign.Client client, @Qualifier("cxfObjectMapper") ObjectMapper mapper) {
        JAXRS2ProfileExtended profile = new JAXRS2ProfileExtended(mapper);
        profile.retryer(retryer).client(client);
        return profile;
    }

    private static class BeanParamEncoderExtended implements Encoder {

        private static final Field VALUES_FIELD;
        static {
            try {
                VALUES_FIELD = EncoderContext.class.getDeclaredField("values");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Can't locate required 'values' field", e);
            }
            VALUES_FIELD.setAccessible(true);
        }

        final Encoder beanParamEncoderBase;
        final ObjectMapper mapper;

        private BeanParamEncoderExtended(Encoder beanParamEncoderBase, ObjectMapper mapper) {
            this.beanParamEncoderBase = beanParamEncoderBase;
            this.mapper = mapper;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void encode(Object object, Type bodyType, RequestTemplate template) {
            if (object instanceof EncoderContext) {
                Map<String, Object> values;
                try {
                    values = (Map<String, Object>) VALUES_FIELD.get(object);
                } catch (IllegalAccessException e) {
                    throw new EncodeException("Can't access required field for transforming", e);
                }
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    if (entry.getValue() == null)
                        continue;
                    if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                        String transformed;
                        try {
                            transformed = mapper.writeValueAsString(entry.getValue());
                        } catch (JsonProcessingException e) {
                            throw new EncodeException("Can't transform map via jackson", e);
                        }
                        entry.setValue(transformed);
                    }
                }
            }
            beanParamEncoderBase.encode(object, bodyType, template);
        }
    }

    @SuppressWarnings("unchecked")
    private static class JAXRS2ProfileExtended extends Feign.Builder {

        private static final Constructor<? extends Encoder> BEAN_PARAM_ENCODER_CONSTRUCTOR;
        private static final Constructor<? extends InvocationHandlerFactory> BEAN_PARAM_INVOCATION_HANDLER_FACTORY_CONSTRUCTOR;
        static {
            try {
                BEAN_PARAM_ENCODER_CONSTRUCTOR = (Constructor<? extends Encoder>) Class.forName("com.qualys.feign.jaxrs.BeanParamEncoder").getConstructor(Encoder.class);
                BEAN_PARAM_INVOCATION_HANDLER_FACTORY_CONSTRUCTOR = (Constructor<? extends InvocationHandlerFactory>) Class.forName("com.qualys.feign.jaxrs.BeanParamInvocationHandlerFactory").getConstructor(InvocationHandlerFactory.class);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw fail(e);
            }
            BEAN_PARAM_ENCODER_CONSTRUCTOR.setAccessible(true);
            BEAN_PARAM_INVOCATION_HANDLER_FACTORY_CONSTRUCTOR.setAccessible(true);
        }

        private final ObjectMapper mapper;

        private JAXRS2ProfileExtended(ObjectMapper mapper) {
            this.mapper = mapper;
            try {
                super.invocationHandlerFactory(BEAN_PARAM_INVOCATION_HANDLER_FACTORY_CONSTRUCTOR.newInstance(new InvocationHandlerFactory.Default()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw fail(e);
            }
        }

        @Override
        public Feign.Builder encoder(Encoder encoder) {
            try {
                return super.encoder(new BeanParamEncoderExtended(BEAN_PARAM_ENCODER_CONSTRUCTOR.newInstance(encoder), mapper));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw fail(e);
            }
        }

        @Override
        public Feign.Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            try {
                return super.invocationHandlerFactory(BEAN_PARAM_INVOCATION_HANDLER_FACTORY_CONSTRUCTOR.newInstance(invocationHandlerFactory));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw fail(e);
            }
        }

        private static IllegalStateException fail(ReflectiveOperationException e) {
            return new IllegalStateException("Can't initialize extended jaxrs2 profile", e);
        }

    }

}
