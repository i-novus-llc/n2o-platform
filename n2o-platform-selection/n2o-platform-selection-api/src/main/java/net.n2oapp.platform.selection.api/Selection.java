package net.n2oapp.platform.selection.api;

/**
 * @param <E> Type for this selection
 */
public interface Selection<E> {
    default E typeMarker() {
        return null;
    }
}
