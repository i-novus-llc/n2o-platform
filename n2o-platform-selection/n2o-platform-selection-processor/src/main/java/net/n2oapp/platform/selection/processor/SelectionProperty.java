package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.LinkedList;
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

    String resolveGenerics(SelectionMeta against) {
        if (nestedGenericSignature.isEmpty())
            return "";
        GenericSignature signature = owner.getGenericSignature();
        if (signature.isEmpty())
            return nestedGenericSignature;
        String resolved = nestedGenericSignature;
        Map<String, String> resolvedVariables = new HashMap<>(0);
        int index = 0;
        for (String var : signature.getVars(true)) {
            int i = 0;
            int j;
            do {
                i = resolved.indexOf(var, i);
                if (i == -1)
                    break;
                j = i + var.length();
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
                if (skip) {
                    i = j;
                    continue;
                }
                int finalIndex = index;
                String bounds = resolvedVariables.computeIfAbsent(var, variable -> {
                    LinkedList<SelectionMeta> stack = new LinkedList<>();
                    SelectionMeta curr = against;
                    do {
                        stack.push(curr);
                        if (curr.getParent() == owner)
                            break;
                        curr = curr.getParent();
                    } while (true);
                    String res = null;
                    while (!stack.isEmpty()) {
                        curr = stack.pop();
                        String extendsSignature = curr.getExtendsSignatureNoBrackets();
                        String[] split = extendsSignature.split(",");
                        res = split[finalIndex].strip();
                        if (!curr.getGenericSignature().containsTypeVariable(res))
                            break;
                    }
                    return res;
                });
                String left = i > 0 ? resolved.substring(0, i) : "";
                String right = j < resolved.length() ? resolved.substring(j) : "";
                resolved = left + bounds + right;
                i = j;
            } while (true);
            index++;
        }
        return resolved;
    }

}
