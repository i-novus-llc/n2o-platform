package net.n2oapp.platform.selection.processor;

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
        this.typeVariables = new ArrayList<>();
        this.upperBounds = new ArrayList<>();
    }

    void addTypeVariable(String var, String upperBound) {
        this.typeVariables.add(var);
        if (upperBound != null) {
            this.upperBounds.add(Arrays.stream(upperBound.split("&")).filter(s -> !s.equals("java.lang.Object")).toArray(String[]::new));
        } else
            upperBounds.add(new String[0]);
    }

    GenericSignature copy() {
        GenericSignature copy = new GenericSignature(owner);
        copy.typeVariables.addAll(this.typeVariables);
        copy.upperBounds.addAll(this.upperBounds);
        copy.selfVariable = this.selfVariable;
        return copy;
    }

    boolean isEmpty() {
        return typeVariables.isEmpty();
    }

    String getSelfVariable() {
        return selfVariable;
    }

    void createSelfVariable() {
        String var = allocateVar("T", "E", "M");
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

    String allocateVar(String...preferred) {
        for (String var : preferred) {
            if (!typeVariables.contains(var))
                return var;
        }
        String var = "T";
        for (int i = 0;;i++) {
            String temp = var + i;
            if (!typeVariables.contains(temp))
                return temp;
        }
    }

    boolean noGenericsDeclared() {
        if (selfVariable == null)
            return typeVariables.isEmpty();
        return typeVariables.size() - 1 == 0;
    }

    String[] getVariableBounds(String var) {
        for (int i = 0; i < typeVariables.size(); i++) {
            if (typeVariables.get(i).equals(var))
                return upperBounds.get(i);
        }
        throw new IllegalStateException("Type variable " + var + " not found");
    }

    int size() {
        return typeVariables.size();
    }

    public boolean containsTypeVariable(CharSequence seq) {
        return typeVariables.stream().anyMatch(s -> s.contentEquals(seq));
    }

    public int indexOf(String var) {
        for (int i = 0; i < typeVariables.size(); i++) {
            if (typeVariables.get(i).equals(var))
                return i;
        }
        throw new IllegalStateException("Type variable " + var + " not found");
    }

}
