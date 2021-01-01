package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;

class SelectionProperty {

    private final String key;
    private final String nestedGenericSignature;
    private final SelectionMeta nestedSelection;
    private final String originalType;
    private final TypeMirror collectionRawType;

    SelectionProperty(String key) {
        this(key, null, null, null, null);
    }

    SelectionProperty(String key, String nestedGenericSignature, SelectionMeta nestedSelection, TypeMirror originalType, TypeMirror collectionRawType) {
        this.key = key;
        this.nestedGenericSignature = nestedGenericSignature;
        this.nestedSelection = nestedSelection;
        if (originalType != null) {
            this.originalType = stripAnnotations(originalType);
        } else
            this.originalType = null;
        this.collectionRawType = collectionRawType;
    }

    private String stripAnnotations(TypeMirror originalType) {
        String type = originalType.toString();
        StringBuilder builder = new StringBuilder();
        boolean anno = false;
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (anno) {
                if (c == ',' || Character.isWhitespace(c))
                    anno = false;
            } else {
                if (c == '@')
                    anno = true;
                else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
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

    String getOriginalType() {
        return originalType;
    }

}
