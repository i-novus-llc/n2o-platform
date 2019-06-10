package net.n2oapp.platform.jaxrs;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
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
        if (Map.class.isAssignableFrom(rawType)){
            //noinspection unchecked
            return (ParamConverter<T>) new MapConverter(genericType);
        } else if (List.class.isAssignableFrom(rawType)) {
            //noinspection unchecked
            return (ParamConverter<T>) new ListConverter(genericType);
        }
        //noinspection unchecked
        return (ParamConverter<T>) converters.stream().filter(c -> c.getType().equals(rawType)).findAny().orElse(null);
    }


}
