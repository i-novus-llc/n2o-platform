package net.n2oapp.platform.selection.api;

/**
 * Просто (true / false), но с одним символом на значение.
 * Используется в http параметрах запроса вместо обычных (true / false), чтобы сэкономить место в строке URI.
 */
public enum SelectionEnum {

    T, F;

    public boolean asBoolean() {
        return this == T;
    }

}
