package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SelectionProperty {

    private static final Set<Character> GENERIC_DELIMITER = Set.of('<', '>', ',', ' ');

    private final String key;
    private final String nestedGenericSignature;
    private final SelectionMeta nestedSelection;
    private final String originalType;
    private final TypeMirror collectionRawType;
    private final TypeMirror type;
    private final SelectionMeta owner;
    private final boolean isJoined;
    private final boolean withNestedJoiner;
    private final boolean rawUse;

    SelectionProperty(String key) {
        this(key, null, null, null, null, null, null, false, false, false);
    }

    SelectionProperty(String key, String nestedGenericSignature, SelectionMeta nestedSelection, TypeMirror type, TypeMirror originalType, TypeMirror collectionRawType, SelectionMeta owner, boolean isJoined, boolean withNestedJoiner, boolean rawUse) {
        this.key = key;
        this.nestedGenericSignature = nestedGenericSignature;
        this.nestedSelection = nestedSelection;
        this.type = type;
        this.owner = owner;
        this.isJoined = isJoined;
        this.withNestedJoiner = withNestedJoiner;
        this.rawUse = rawUse;
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

    String getNestedGenericSignatureOrWildcards(String...additionalTypeVariables) {
        if (nestedGenericSignature.isBlank() && !rawUse)
            return additionalTypeVariables.length == 0 ? "" : "<" + String.join(", ", additionalTypeVariables) + ">";
        String suffix = additionalTypeVariables.length == 0 ? "" : ", " + String.join(", ", additionalTypeVariables);
        if (!nestedGenericSignature.isEmpty())
            return "<" + String.join(", ", nestedGenericSignature) + suffix + ">";
        return "<" + IntStream.range(0, nestedSelection.getGenericSignature().size()).mapToObj(ignored -> "?").collect(Collectors.joining(", ")) + suffix + ">";
    }

    TypeMirror getCollectionRawType() {
        return collectionRawType;
    }

    SelectionMeta getNestedSelection() {
        return nestedSelection;
    }

    TypeMirror getType() {
        return type;
    }

    String getOriginalType() {
        return originalType;
    }

    String resolveTypeVariables(SelectionMeta against) {
        return resolveTypeVariables(owner, against, nestedGenericSignature);
    }

    private String resolveTypeVariables(SelectionMeta from, SelectionMeta against, String generics) {
        if (from == against || generics.isEmpty() || from.getGenericSignature().isEmpty())
            return generics;
        Map<String, String> resolvedVariables = new HashMap<>(0);
        StringBuilder temp = new StringBuilder();
        StringBuilder resolved = new StringBuilder();
        GenericSignature signature = from.getGenericSignature();
        for (int i = 0; i < generics.length(); i++) {
            char c = generics.charAt(i);
            temp.append(c);
            if (signature.containsTypeVariable(temp) && !shouldSkip(generics, i)) {
                String var = temp.toString();
                String bounds = resolvedVariables.computeIfAbsent(var, variable ->
                    findTypeVariableBounds(from, against, signature.indexOf(var))
                );
                resolved.append(bounds);
                temp.setLength(0);
            } else if (GENERIC_DELIMITER.contains(c)) {
                resolved.append(temp);
                temp.setLength(0);
            }
        }
        if (temp.length() > 0)
            resolved.append(temp);
        return resolved.toString();
    }

    private String findTypeVariableBounds(SelectionMeta from, SelectionMeta against, int index) {
        SelectionMeta curr = against;
        do {
            if (curr.getParent() == from)
                return resolveTypeVariables(curr, against, getExtendsBounds(curr.getExtendsSignatureNoBrackets(), index).strip());
            curr = curr.getParent();
        } while (true);
    }

    private String getExtendsBounds(String signature, int index) {
        int numBrackets = 0;
        int currIndex = 0;
        int prev = 0;
        for (int i = 0; i < signature.length(); i++) {
            char c = signature.charAt(i);
            if (c == ',' && numBrackets == 0) {
                if (currIndex == index) {
                    return signature.substring(prev, i);
                }
                currIndex++;
                prev = i + 1;
            } else if (c == '<') {
                numBrackets++;
            } else if (c == '>')
                numBrackets--;
        }
        return signature.substring(prev);
    }

    private boolean shouldSkip(String signature, int i) {
        if (i < signature.length() - 1) {
            return !GENERIC_DELIMITER.contains(signature.charAt(i + 1));
        }
        return false;
    }

    boolean isJoined() {
        return isJoined;
    }

    boolean isWithNestedJoiner() {
        return withNestedJoiner;
    }

}
