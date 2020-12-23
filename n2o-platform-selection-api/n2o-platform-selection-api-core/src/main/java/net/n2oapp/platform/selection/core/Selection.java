package net.n2oapp.platform.selection.core;

/**
 * @param <E> Type for this selection
 */
public interface Selection<E> {
    default E marker() {
        return null;
    }
}
