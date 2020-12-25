package net.n2oapp.platform.selection.processor;

class SelectionProperty {

    private final String key;
    private final String fieldTypeSignature; // null if property is not nested selection

    SelectionProperty(String key) {
        this(key, null);
    }

    SelectionProperty(String key, String fieldTypeSignature) {
        this.key = key;
        this.fieldTypeSignature = fieldTypeSignature;
    }

}
