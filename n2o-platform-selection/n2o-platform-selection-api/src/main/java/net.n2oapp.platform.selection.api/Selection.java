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

    static <E> String encode(Selection<E> selection) {
        if (selection == null)
            return null;
        try {
            String json = Util.MAPPER.writeValueAsString(selection);
            return Util.encode(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    static <E, S extends Selection<E>> S decode(String encodedJson, Class<S> target) {
        if (encodedJson == null || encodedJson.isBlank())
            return null;
        try {
            return Util.MAPPER.readValue(Util.decode(encodedJson), target);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
