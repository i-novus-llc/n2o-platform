package net.n2oapp.platform.jaxrs;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
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
            MapConverter mapConverter;
            if (genericType instanceof ParameterizedType &&
                    ((ParameterizedType) genericType).getActualTypeArguments().length >= 2 &&
                    ((ParameterizedType) genericType).getActualTypeArguments()[1] instanceof Class)
                mapConverter = new MapConverter((Class) ((ParameterizedType) genericType).getActualTypeArguments()[1]);
            else mapConverter = new MapConverter(Object.class);
            //noinspection unchecked
            return (ParamConverter<T>) mapConverter;
        } else if (List.class.isAssignableFrom(rawType)) {
            ListConverter listConverter;
            if (genericType instanceof ParameterizedType &&
                    ((ParameterizedType) genericType).getActualTypeArguments().length > 0 &&
                    ((ParameterizedType) genericType).getActualTypeArguments()[0] instanceof Class)
                listConverter = new ListConverter((Class) ((ParameterizedType) genericType).getActualTypeArguments()[0]);
            else listConverter = new ListConverter(Object.class);
            //noinspection unchecked
            return (ParamConverter<T>) listConverter;
        }
        //noinspection unchecked
        return (ParamConverter<T>) converters.stream().filter(c -> c.getType().equals(rawType)).findAny().orElse(null);
    }


}
