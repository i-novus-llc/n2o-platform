package net.n2oapp.platform.selection.unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Util {

    public static <E1, E2> E2 mapNullable(E1 e1, Function<? super E1, ? extends E2> mapper) {
        return e1 == null ? null : mapper.apply(e1);
    }

    public static <E1, E2> List<E2> mapCollection(
        Collection<? extends E1> src,
        Function<? super E1, ? extends E2> mapper
    ) {
        return mapCollection(src, mapper, ArrayList::new);
    }

    public static <E1, E2, C extends Collection<E2>> C mapCollection(
        Collection<? extends E1> src,
        Function<? super E1, ? extends E2> mapper,
        Supplier<? extends C> collectionSupplier
    ) {
        return src == null ? null : src.stream().map(mapper).collect(Collectors.toCollection(collectionSupplier));
    }

}
