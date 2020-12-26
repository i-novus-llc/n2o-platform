package net.n2oapp.platform.selection.processor;

class SelectionProperty {

    private final String key;
    private final String nestedGenericSignature; // null if property is not nested selection
    private final SelectionMeta nestedSelection;

    SelectionProperty(String key) {
        this(key, null, null);
    }

    SelectionProperty(String key, String nestedGenericSignature, SelectionMeta nestedSelection) {
        this.key = key;
        this.nestedGenericSignature = nestedGenericSignature;
        this.nestedSelection = nestedSelection;
    }

    String getKey() {
        return key;
    }

    String getNestedGenericSignature() {
        if (nestedGenericSignature.isEmpty())
            return "";
        return "<" + nestedGenericSignature + ">";
    }

    SelectionMeta getNestedSelection() {
        return nestedSelection;
    }

}
