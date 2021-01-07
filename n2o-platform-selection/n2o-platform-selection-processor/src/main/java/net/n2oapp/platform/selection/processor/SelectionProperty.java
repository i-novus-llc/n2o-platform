package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class SelectionProperty {

    private static final Set<Character> GENERIC_DELIMITER = Set.of('<', '>', ',', ' ');

    private final String key;
    private final String nestedGenericSignature;
    private final SelectionMeta nestedSelection;
    private final String originalType;
    private final TypeMirror collectionRawType;
    private final SelectionMeta owner;

    SelectionProperty(String key) {
        this(key, null, null, null, null, null);
    }

    SelectionProperty(String key, String nestedGenericSignature, SelectionMeta nestedSelection, TypeMirror originalType, TypeMirror collectionRawType, SelectionMeta owner) {
        this.key = key;
        this.nestedGenericSignature = nestedGenericSignature;
        this.nestedSelection = nestedSelection;
        this.owner = owner;
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

    String resolveTypeVariables(SelectionMeta against) {
        return resolveTypeVariables(owner, against, nestedGenericSignature);
    }

    private String resolveTypeVariables(SelectionMeta from, SelectionMeta against, String resolved) {
        if (from == against || resolved.isEmpty() || from.getGenericSignature().isEmpty())
            return resolved;
        Map<String, String> resolvedVariables = new HashMap<>(0);
        int index = 0;
        for (String var : from.getGenericSignature().getVars(true)) {
            int i = 0;
            int j;
            do {
                i = resolved.indexOf(var, i);
                if (i == -1)
                    break;
                j = i + var.length();
                if (shouldSkip(resolved, i, j)) {
                    i = j;
                    continue;
                }
                final int finalIndex = index;
                String bounds = resolvedVariables.computeIfAbsent(var, variable ->
                    findTypeVariableBounds(from, against, finalIndex)
                );
                if (!bounds.equals(var)) {
                    String left = i > 0 ? resolved.substring(0, i) : "";
                    String right = j < resolved.length() ? resolved.substring(j) : "";
                    resolved = left + bounds + right;
                }
                i = j + bounds.length() - 1;
            } while (true);
            index++;
        }
        return resolved;
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

    private boolean shouldSkip(String resolved, int i, int j) {
        boolean skip = false;
        if (i == 0) {
            if (j < resolved.length()) {
                skip = !GENERIC_DELIMITER.contains(resolved.charAt(j));
            }
        } else {
            skip = !GENERIC_DELIMITER.contains(resolved.charAt(i - 1));
            if (j < resolved.length())
                skip |= !GENERIC_DELIMITER.contains(resolved.charAt(j));
        }
        return skip;
    }

}
