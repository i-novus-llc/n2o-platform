package net.n2oapp.platform.jaxrs;

import org.springframework.data.domain.Sort;

/**
 * Конвертация сортировки в параметрах REST запроса
 */
public class OrderParameterConverter implements TypedParamConverter<Sort.Order> {

    @Override
    public Class<Sort.Order> getType() {
        return Sort.Order.class;
    }

    @Override
    public Sort.Order fromString(String value) {
        final String[] split;
        if (value.contains(":")) {
            split = value.split(": "); // Так отправляет сортировку n2o
        } else {
//          Так раньше отправлял сортировку данный конвертер,
//          а так же в некоторых проектах используется такой формат
            split = value.split(",");
        }
        return new Sort.Order(
            Sort.Direction.fromString(split[1]),
            split[0]
        );
    }

    @Override
    public String toString(Sort.Order value) {
        return value.getProperty() + ": " + value.getDirection().name().toLowerCase();
    }

}
