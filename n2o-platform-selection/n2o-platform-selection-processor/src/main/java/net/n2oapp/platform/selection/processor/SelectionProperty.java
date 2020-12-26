package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;

class SelectionProperty {

    private final String key;
    private final String nestedGenericSignature;
    private final SelectionMeta nestedSelection;
    private final TypeMirror collectionType;

    SelectionProperty(String key) {
        this(key, null, null, null);
    }

    SelectionProperty(String key, String nestedGenericSignature, SelectionMeta nestedSelection, TypeMirror collectionType) {
        this.key = key;
        this.nestedGenericSignature = nestedGenericSignature;
        this.nestedSelection = nestedSelection;
        this.collectionType = collectionType;
    }

    String getKey() {
        return key;
    }

    String getNestedGenericSignature() {
        if (nestedGenericSignature.isEmpty())
            return "";
        return "<" + nestedGenericSignature + ">";
    }

    TypeMirror getCollectionType() {
        return collectionType;
    }

    SelectionMeta getNestedSelection() {
        return nestedSelection;
    }

}
