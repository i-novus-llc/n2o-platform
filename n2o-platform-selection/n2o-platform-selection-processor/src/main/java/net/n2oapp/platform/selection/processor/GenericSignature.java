package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GenericSignature {

    private final List<String> imports;
    private final List<String> typeVariables;
    private final List<String[]> upperBounds;
    private final List<Integer> sameAsOwnerPositions;
    private final TypeElement owner;

    private boolean importsOnly;
    private String selfVariable;

    GenericSignature(TypeElement owner) {
        this.owner = owner;
        this.imports = new ArrayList<>(0);
        this.typeVariables = new ArrayList<>(0);
        this.upperBounds = new ArrayList<>(0);
        this.sameAsOwnerPositions = new ArrayList<>(0);
        this.importsOnly = false;
    }

    void appendImport(String imp) {
        if (!imp.startsWith("java.lang."))
            this.imports.add(imp);
    }

    void addTypeVariable(String var, String upperBound) {
        this.typeVariables.add(var);
        this.upperBounds.add(Arrays.stream(upperBound.split("&")).filter(s -> !s.equals("java.lang.Object")).toArray(String[]::new));
    }

    void setImportsOnly() {
        this.importsOnly = true;
    }

    boolean isImportsOnly() {
        return importsOnly;
    }

    Element getOwner() {
        return owner;
    }

    String getSelfVariable() {
        return selfVariable;
    }

    List<String> getImports() {
        return imports;
    }

    void createSelfVariable() {
        String var = allocateVar();
        StringBuilder builder = new StringBuilder();
        builder.append(owner.getQualifiedName());
        builder.append("<").append(var);
        for (String otherVar : typeVariables) {
            builder.append(", ").append(otherVar);
        }
        builder.append(">");
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

}
