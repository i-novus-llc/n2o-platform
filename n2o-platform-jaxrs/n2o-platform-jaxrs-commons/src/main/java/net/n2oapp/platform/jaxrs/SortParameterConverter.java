package net.n2oapp.platform.jaxrs;

import org.springframework.data.domain.Sort;

/**
 * Конвертация сортировки в параметрах REST запроса
 */
public class SortParameterConverter implements TypedParamConverter<Sort.Order> {
    @Override
    public Class<Sort.Order> getType() {
        return Sort.Order.class;
    }

    @Override
    public Sort.Order fromString(String value) {
        if (value.contains(":")) {
            String fst = value.split(",")[0];
            String[] cNd = fst.split(": ");
            return new Sort.Order(Sort.Direction.fromString(cNd[1]), cNd[0]);
        } else {
            String[] cNd = value.split(",");
            return new Sort.Order(Sort.Direction.fromString(cNd[1]), cNd[0]);
        }
    }

    @Override
    public String toString(Sort.Order value) {
        return value.getProperty() + "," + value.getDirection().name().toLowerCase();
    }
}
