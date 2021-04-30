package net.n2oapp.platform.seek;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.NullHandling.*;

public final class SortUtil {

    private SortUtil() {
        throw new UnsupportedOperationException();
    }

    public static List<Sort.Order> flip(@NonNull List<? extends Sort.Order> orders) {
        List<Sort.Order> result = new ArrayList<>();
        for (Sort.Order order : orders) {
            result.add(flip(order));
        }
        return result;
    }

    public static Sort flip(@NonNull Sort sort) {
        return Sort.by(sort.map(SortUtil::flip).stream().collect(Collectors.toList()));
    }

    public static Sort.Order flip(@NonNull Sort.Order order) {
        Sort.NullHandling handling = order.getNullHandling();
        if (handling != NATIVE)
            handling = handling == NULLS_FIRST ? NULLS_LAST : NULLS_FIRST;
        return new Sort.Order(order.getDirection() == ASC ? DESC : ASC, order.getProperty(), handling);
    }

}
