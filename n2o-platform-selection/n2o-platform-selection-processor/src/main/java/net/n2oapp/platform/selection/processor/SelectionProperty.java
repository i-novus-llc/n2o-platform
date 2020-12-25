package net.n2oapp.platform.selection.processor;

class SelectionProperty {

    private final String key;
    private final String fieldTypeSignature; // null if property is not nested selection
    private final SelectionMeta nestedSelection;

    SelectionProperty(String key) {
        this(key, null, null);
    }

    SelectionProperty(String key, String fieldTypeSignature, SelectionMeta nestedSelection) {
        this.key = key;
        this.fieldTypeSignature = fieldTypeSignature;
        this.nestedSelection = nestedSelection;
    }

}
