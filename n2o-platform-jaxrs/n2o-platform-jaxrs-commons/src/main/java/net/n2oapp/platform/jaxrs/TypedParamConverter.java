package net.n2oapp.platform.jaxrs;

import javax.ws.rs.ext.ParamConverter;

/**
 * Типизированный конвертер параметров REST запроса
 * @param <T> Тип конвертируемых данных
 */
public interface TypedParamConverter<T> extends ParamConverter<T> {
    /**
     * Получить тип конвертируемых данных
     */
    Class<T> getType();
}
