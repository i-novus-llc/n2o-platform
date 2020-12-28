package net.n2oapp.platform.selection.api;

/**
 * @param <E> Type for this selection
 */
public interface Selection<E> {

    /**
     * Marker method to ensure type-safety
     */
    default E typeMarker() {
        return null;
    }

    /**
     * @see SelectionPropagationEnum
     */
    default SelectionPropagationEnum propagation() {
        return SelectionPropagationEnum.NORMAL;
    }

}
