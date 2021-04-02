package net.n2oapp.platform.jaxrs;

import org.springframework.data.domain.Sort;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class SortParameterConverter implements TypedParamConverter<Sort> {

    private final OrderParameterConverter delegate = new OrderParameterConverter();

    @Override
    public Class<Sort> getType() {
        return Sort.class;
    }

    @Override
    public Sort fromString(final String value) {
        return Sort.by(Arrays.stream(value.split("-")).map(delegate::fromString).collect(toList()));
    }

    @Override
    public String toString(final Sort value) {
        return value.stream().map(delegate::toString).collect(joining("-"));
    }

}
