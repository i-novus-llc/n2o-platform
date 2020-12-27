package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;

class SelectionProperty {

    private final String key;
    private final String nestedGenericSignature;
    private final SelectionMeta nestedSelection;
    private final TypeMirror originalType;
    private final TypeMirror collectionRawType;

    SelectionProperty(String key) {
        this(key, null, null, null, null);
    }

    SelectionProperty(String key, String nestedGenericSignature, SelectionMeta nestedSelection, TypeMirror originalType, TypeMirror collectionRawType) {
        this.key = key;
        this.nestedGenericSignature = nestedGenericSignature;
        this.nestedSelection = nestedSelection;
        this.originalType = originalType;
        this.collectionRawType = collectionRawType;
    }

    String getKey() {
        return key;
    }

    String getNestedGenericSignature() {
        if (nestedGenericSignature.isEmpty())
            return "";
        return "<" + nestedGenericSignature + ">";
    }

    TypeMirror getCollectionRawType() {
        return collectionRawType;
    }

    SelectionMeta getNestedSelection() {
        return nestedSelection;
    }

    TypeMirror getOriginalType() {
        return originalType;
    }

}
