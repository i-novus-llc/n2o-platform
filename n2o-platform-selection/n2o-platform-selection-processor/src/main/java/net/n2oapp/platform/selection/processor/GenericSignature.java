package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class GenericSignature {

    private final List<String> typeVariables;
    private final List<String[]> upperBounds;
    private final List<Integer> sameAsOwnerPositions;
    private final TypeElement owner;

    private boolean markOnly;
    private String selfVariable;

    GenericSignature(TypeElement owner) {
        this.owner = owner;
        this.typeVariables = new ArrayList<>(0);
        this.upperBounds = new ArrayList<>(0);
        this.sameAsOwnerPositions = new ArrayList<>(0);
        this.markOnly = false;
    }

    void addTypeVariable(String var, String upperBound) {
        this.typeVariables.add(var);
        this.upperBounds.add(Arrays.stream(upperBound.split("&")).filter(s -> !s.equals("java.lang.Object")).toArray(String[]::new));
    }

    boolean isEmpty() {
        return typeVariables.isEmpty();
    }

    void setMarkOnly() {
        this.markOnly = true;
    }

    boolean isMarkOnly() {
        return markOnly;
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
        if (!typeVariables.isEmpty()) {
            builder.append("<");
            for (Iterator<String> iter = typeVariables.iterator(); iter.hasNext(); ) {
                String otherVar = iter.next();
                builder.append(otherVar);
                if (iter.hasNext())
                    builder.append(',');
            }
            builder.append(">");
        }
        typeVariables.add(0, var);
        upperBounds.add(0, new String[] {builder.toString()});
        this.selfVariable = var;
        for (int idx : sameAsOwnerPositions) {
            String[] bounds = upperBounds.get(idx);
            for (int i = 0; i < bounds.length; i++) {
                if (bounds[i].startsWith(owner.getQualifiedName().toString()) && bounds[i].endsWith(">")) {
                    bounds[i] = bounds[i].substring(0, bounds[i].length() - 1) + ", " + var + ">";
                }
            }
        }
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

    void markLastSame() {
        this.sameAsOwnerPositions.add(typeVariables.size() - 1);
    }

    int sizeWithoutSelfVariable() {
        if (selfVariable == null)
            return typeVariables.size();
        return typeVariables.size() - 1;
    }

}
