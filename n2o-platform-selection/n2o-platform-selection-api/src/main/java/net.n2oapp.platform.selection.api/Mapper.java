package net.n2oapp.platform.selection.api;

/**
 * @param <E> Type for this mapper
 */
public interface Mapper<E> {

    /**
     * @return Empty model (DTO), whose fields will be selectively mapped according to {@link Selection<E>}
     */
    E create();

}
