package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SelectionProperty {

    private final String name;
    private final String generics;
    private final SelectionMeta selection;
    private final String originalTypeStr;
    private final TypeMirror originalType;
    private final String typeStr;
    private final Element member;
    private final TypeMirror collectionType;
    private final boolean isJoined;
    private final boolean withNestedJoiner;

    SelectionProperty(
        String name,
        TypeMirror originalType,
        Element member,
        boolean joined
    ) {
        this(name, member, originalType, null, null, null, null, joined, false);
    }

    SelectionProperty(
        String name,
        Element member,
        TypeMirror originalType,
        TypeMirror type,
        SelectionMeta selection,
        String generics,
        TypeMirror collectionType,
        boolean isJoined,
        boolean withNestedJoiner
    ) {
        this.originalType = originalType;
        this.name = name;
        this.generics = generics;
        this.selection = selection;
        this.member = member;
        if (type != null)
            this.typeStr = stripAnnotations(type);
        else
            this.typeStr = null;
        this.isJoined = isJoined;
        this.withNestedJoiner = withNestedJoiner;
        if (originalType != null) {
            this.originalTypeStr = stripAnnotations(originalType);
        } else
            this.originalTypeStr = null;
        this.collectionType = collectionType;
    }

    private String stripAnnotations(TypeMirror originalType) {
        String t = originalType.toString();
        StringBuilder builder = new StringBuilder();
        boolean anno = false;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
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

    String getName() {
        return name;
    }

    String getGenerics(String...additionalGenerics) {
        String result;
        additionalGenerics = Arrays.stream(additionalGenerics).filter(Objects::nonNull).toArray(String[]::new);
        if (generics.isBlank()) {
            result = additionalGenerics.length == 0 ? "" : "<" + join(additionalGenerics) + ">";
        } else {
            String suffix = additionalGenerics.length == 0 ? "" : ", " + join(additionalGenerics);
            if (!generics.isEmpty()) {
                result = "<" + generics + suffix + ">";
            } else {
                result = "<" + makeWildcards(selection.getSelectionGenericSignature().size()) + suffix + ">";
            }
        }
        return result;
    }

    private String makeWildcards(int count) {
        return IntStream.range(0, count).mapToObj(unused -> "?").collect(Collectors.joining(", "));
    }

    private String join(String[] generics) {
        return String.join(", ", generics);
    }

    TypeMirror getCollectionType() {
        return collectionType;
    }

    SelectionMeta getSelection() {
        return selection;
    }

    boolean selective() {
        return selection != null;
    }

    String getTypeStr() {
        return typeStr;
    }

    TypeMirror getOriginalType() {
        return originalType;
    }

    String getOriginalTypeStr() {
        return originalTypeStr;
    }

    Element getMember() {
        return member;
    }

    boolean isJoined() {
        return isJoined;
    }

    boolean isWithNestedJoiner() {
        return withNestedJoiner;
    }

}
