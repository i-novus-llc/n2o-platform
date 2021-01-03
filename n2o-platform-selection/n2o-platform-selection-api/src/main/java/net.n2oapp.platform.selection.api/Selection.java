package net.n2oapp.platform.selection.api;

import com.fasterxml.jackson.core.JsonProcessingException;

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

    static <E> String toString(Selection<E> selection) {
        if (selection == null)
            return null;
        try {
            return Util.MAPPER.writeValueAsString(selection);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    static <E, S extends Selection<E>> S parse(String str, Class<S> target) {
        if (str == null || str.isBlank())
            return null;
        try {
            return Util.MAPPER.readValue(str, target);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
