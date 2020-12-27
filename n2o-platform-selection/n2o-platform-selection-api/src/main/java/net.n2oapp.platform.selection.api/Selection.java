package net.n2oapp.platform.selection.api;

/**
 * @param <E> Type for this selection
 */
public interface Selection<E> {

    default E typeMarker() {
        return null;
    }

    /**
     * @return
     *  Whether selection should be entirely disabled (i.e., corresponding mapper will select all fields of {@link E})
     *  Return value will not propagate through nested selections.
     */
    default boolean selectAll() {
        return true;
    }

}
