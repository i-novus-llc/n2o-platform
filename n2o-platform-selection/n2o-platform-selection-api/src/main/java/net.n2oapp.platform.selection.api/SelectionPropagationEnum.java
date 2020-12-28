package net.n2oapp.platform.selection.api;

public enum SelectionPropagationEnum {

    /**
     * Selection is entirely defined by {@link SelectionEnum} values.
     * This is the default value.
     */
    NORMAL,

    /**
     * Selection is disabled at this particular level of nesting,
     * i.e., corresponding mapper will select all fields.
     * This will not propagate through nested selections.
     */
    ALL,

    /**
     * Selection is entirely disabled at this and any nested selection.
     */
    NESTED

}
