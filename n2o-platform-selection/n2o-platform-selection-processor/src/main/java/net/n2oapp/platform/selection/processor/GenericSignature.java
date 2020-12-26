package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GenericSignature {

    private final List<String> typeVariables;
    private final List<String[]> upperBounds;
    private final TypeElement owner;

    private String selfVariable;

    GenericSignature(TypeElement owner) {
        this.owner = owner;
        this.typeVariables = new ArrayList<>(0);
        this.upperBounds = new ArrayList<>(0);
    }

    void addTypeVariable(String var, String upperBound) {
        this.typeVariables.add(var);
        this.upperBounds.add(Arrays.stream(upperBound.split("&")).filter(s -> !s.equals("java.lang.Object")).toArray(String[]::new));
    }

    boolean isEmpty() {
        return typeVariables.isEmpty();
    }

    Element getOwner() {
        return owner;
    }

    String getSelfVariable() {
        return selfVariable;
    }

    void createSelfVariable() {
        String var = allocateVar();
        StringBuilder builder = new StringBuilder();
        builder.append(owner.getQualifiedName());
        if (!typeVariables.isEmpty())
            builder.append("<").append(String.join(", ", typeVariables)).append(">");
        typeVariables.add(0, var);
        upperBounds.add(0, new String[] {builder.toString()});
        this.selfVariable = var;
    }

    @Override
    public String toString() {
        if (typeVariables.isEmpty())
            return "";
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        for (int i = 0; i < typeVariables.size(); i++) {
            String var = typeVariables.get(i);
            builder.append(var);
            String[] bounds = upperBounds.get(i);
            if (bounds.length > 0) {
                builder.append(" extends ");
                for (int j = 0, boundsLength = bounds.length; j < boundsLength; j++) {
                    String bound = bounds[j];
                    builder.append(bound);
                    if (j < boundsLength - 1) {
                        builder.append(" & ");
                    }
                }
            }
            if (i < typeVariables.size() - 1)
                builder.append(", ");
        }
        builder.append(">");
        return builder.toString();
    }

    String varsToString(boolean includeSelfVariable) {
        if (typeVariables.isEmpty())
            return "";
        if (includeSelfVariable || selfVariable == null)
            return "<" + String.join(", ", typeVariables) + ">";
        return "<" + String.join(", ", typeVariables.subList(1, typeVariables.size())) + ">";
    }

    private String allocateVar() {
        for (int i = 'A'; i <= 'Z'; i++) {
            String var = Character.toString((char) i);
            if (!typeVariables.contains(var))
                return var;
        }
        String var = "T";
        for (int i = 0;;i++) {
            String temp = var + i;
            if (!typeVariables.contains(temp))
                return var;
        }
    }

    int sizeWithoutSelfVariable() {
        if (selfVariable == null)
            return typeVariables.size();
        return typeVariables.size() - 1;
    }

}
