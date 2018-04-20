package net.n2oapp.platform.jaxrs.common;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Поставщик типизированных конвертеров {@link TypedParamConverter} параметров REST сервиса в java типы
 */
@Provider
public class TypedParametersProvider implements ParamConverterProvider {

    private Set<TypedParamConverter<?>> converters;

    public TypedParametersProvider(Set<TypedParamConverter<?>> converters) {
        this.converters = converters;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        return (ParamConverter<T>) converters.stream().filter(c -> c.getType().equals(rawType)).findAny().orElse(null);
    }
}
