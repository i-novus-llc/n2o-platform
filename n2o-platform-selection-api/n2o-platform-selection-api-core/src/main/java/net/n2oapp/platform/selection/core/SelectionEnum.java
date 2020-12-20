package net.n2oapp.platform.selection.core;

/**
 * Just true/false, but with one character per value.
 * Used in http query-parameters instead of usual 'true' / 'false' to save some space in URI string.
 */
public enum SelectionEnum {

    T, F;

    public boolean asBoolean() {
        return this == T;
    }

}
