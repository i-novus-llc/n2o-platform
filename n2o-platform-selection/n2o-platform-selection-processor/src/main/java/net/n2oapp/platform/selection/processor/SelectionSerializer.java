package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.SelectionEnum;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class SelectionSerializer extends AbstractSerializer {

    private static final String CLASS_PREFIX = "Default";
    private static final String GETTER_PREFIX = "getSelect";
    private static final String SELECT_PREFIX = "select";
    private static final String UNSELECT_PREFIX = "unselect";
    private static final String METHOD_START = "\tpublic ";
    private static final String METHOD_END = "\t}\n\n";
    private static final String SELECTION_ARG = "selection";
    private static final String FIELD_START = "\tprivate ";
    private static final String FIELD_END = ";\n\n";

    private final TypeMirror selectionEnum;
    private final TypeMirror selectionInterface;
    private final TypeMirror selectionPropagation;
    private final TypeMirror jsonTypeInfo;
    private final TypeMirror jsonSubTypes;

    private final boolean addJacksonTyping;
    private final boolean addJaxRsAnnotations;

    SelectionSerializer(
            TypeMirror selectionKey,
            TypeMirror selectionEnum,
            TypeMirror selectionInterface,
            TypeMirror selectionPropagation,
            boolean addJacksonTyping,
            boolean addJaxRsAnnotations,
            TypeElement jsonTypeInfo,
            TypeElement jsonSubTypes
    ) {
        super(selectionKey);
        this.selectionEnum = selectionEnum;
        this.selectionInterface = selectionInterface;
        this.selectionPropagation = selectionPropagation;
        this.addJacksonTyping = addJacksonTyping;
        this.addJaxRsAnnotations = addJaxRsAnnotations;
        if (jsonTypeInfo != null) {
            this.jsonTypeInfo = jsonTypeInfo.asType();
            this.jsonSubTypes = jsonSubTypes.asType();
        } else {
            this.jsonTypeInfo = null;
            this.jsonSubTypes = null;
        }
    }

    @Override
    String getSuffix() {
        return "Selection";
    }

    @Override
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append(selectionEnum.toString()).append(' ');
        out.append(GETTER_PREFIX).append(capitalize(property.getKey())).append("();");
        if (property.getNestedSelection() != null) {
            out.append("\n\t");
            appendSelectionKey(out, property.getKey());
            out.append("\n\t");
            SelectionMeta nestedSelection = property.getNestedSelection();
            out.append(getQualifiedName(nestedSelection, nestedSelection.getTargetPackage()));
            out.append(property.getNestedGenericSignature()).append(' ');
            out.append(property.getKey()).append(getSuffix()).append("();");
        }
    }

    @Override
    void postSerialize(SelectionMeta meta, Writer out) throws IOException {
        if (!meta.isAbstract()) {
            String selfDefault = getQualifiedName(meta, meta.getTargetPackage(), CLASS_PREFIX);
            out.append('\n').append(METHOD_START);
            out.append("static ");
            out.append(meta.getGenericSignature().toString());
            out.append(' ');
            out.append(selfDefault);
            out.append(meta.getGenericSignature().varsToString(true));
            out.append(' ').append("create() {\n");
            out.append("\t\treturn new ").append(selfDefault);
            if (!meta.getGenericSignature().isEmpty())
                out.append("<>");
            out.append("();\n");
            out.append(METHOD_END);
        }
    }

    @Override
    void serialize(SelectionMeta meta, Filer filer) throws IOException {
        super.serialize(meta, filer);
        PackageElement targetPackage = meta.getTargetPackage();
        String className = CLASS_PREFIX + meta.getTarget().getSimpleName() + getSuffix();
        String self = getQualifiedName(meta, targetPackage, CLASS_PREFIX);
        JavaFileObject file = filer.createSourceFile(self, targetPackage);
        self += meta.getGenericSignature().varsToString(true);
        try (Writer out = file.openWriter()) {
            appendPackage(targetPackage, out);
            if (addJacksonTyping && !meta.getChildren().isEmpty()) {
                out.append("@").append(jsonTypeInfo.toString()).append("(use = ").append(jsonTypeInfo.toString()).append(".Id.NAME, property = \"t\")\n");
                out.append("@").append(jsonSubTypes.toString()).append("({\n");
                addJacksonTyping(meta, out);
                out.append("})\n");
            }
            out.append("public");
            if (meta.isAbstract())
                out.append(" abstract");
            out.append(" class ").append(className).append(meta.getGenericSignature().toString());
            if (meta.getParent() != null) {
                out.append(" extends ").append(getQualifiedName(meta.getParent(), meta.getParent().getTargetPackage(), CLASS_PREFIX));
                out.append(meta.getExtendsSignature());
            }
            out.append(" implements ").append(getQualifiedName(meta, targetPackage)).append(meta.getGenericSignature().varsToString(true));
            out.append("{\n\n");
            if (meta.getParent() == null) {
                out.append(FIELD_START).append(selectionPropagation.toString()).append(' ').append("propagation").append(FIELD_END);
                appendOverride(out);
                out.append(METHOD_START).append(selectionPropagation.toString()).append(' ').append("propagation() {\n");
                out.append("\t\t").append("return propagation;\n");
                out.append(METHOD_END);
                out.append(METHOD_START).append(self).append(" propagate(").append(selectionPropagation.toString()).append(' ');
                out.append("propagation) {\n");
                out.append("\t\t");
                out.append("this.propagation = propagation;\n");
                out.append("\t\t");
                out.append("return this;\n");
                out.append(METHOD_END);
            }
            for (SelectionProperty property : meta.getProperties()) {
                String capitalizedKey = capitalize(property.getKey());
                out.append(FIELD_START).append(selectionEnum.toString()).append(' ').append(SELECT_PREFIX).append(capitalizedKey).append(FIELD_END);
                appendOverride(out);
                out.append(METHOD_START).append(selectionEnum.toString()).append(' ').append(GETTER_PREFIX);
                out.append(capitalizedKey).append("() {\n");
                out.append("\t\t").append("return ").append(SELECT_PREFIX).append(capitalizedKey).append(";\n");
                out.append(METHOD_END);
                if (property.getNestedSelection() != null) {
                    String nestedQualified = getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage());
                    out.append(FIELD_START).append(nestedQualified).append(property.getNestedGenericSignature());
                    out.append(' ').append(property.getKey()).append(getSuffix()).append(FIELD_END);
                    appendOverride(out);
                    out.append(METHOD_START);
                    out.append(nestedQualified).append(property.getNestedGenericSignature());
                    out.append(' ');
                    out.append(property.getKey()).append(getSuffix()).append("() {\n");
                    out.append("\t\t").append("return ").append(property.getKey()).append(getSuffix()).append(";\n");
                    out.append(METHOD_END);
                }
                String nestedSelectionArg = "";
                String[][] assignments = null;
                if (property.getNestedSelection() != null) {
                    assignments = new String[1][2];
                    assignments[0][0] = property.getKey() + getSuffix();
                    nestedSelectionArg = getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage()) + property.getNestedGenericSignature() + " " + SELECTION_ARG;
                    assignments[0][1] = SELECTION_ARG;
                }
                appendSelect(self, out, capitalizedKey, SELECT_PREFIX, SelectionEnum.T, nestedSelectionArg, assignments);
                if (assignments != null)
                    assignments[0][1] = "null";
                appendSelect(self, out, capitalizedKey, UNSELECT_PREFIX, SelectionEnum.F, "", assignments);
            }
            override(meta, out, self);
            out.append("\n}");
        }
    }

    private void override(SelectionMeta meta, Writer out, String self) throws IOException {
        SelectionMeta parent = meta.getParent();
        while (parent != null) {
            for (SelectionProperty property : parent.getProperties()) {
                out.append(METHOD_START);
                out.append(self).append(meta.getGenericSignature().varsToString(true));
//                out.append(' ').append()
            }
            parent = parent.getParent();
        }
    }

    private void appendSelect(String self, Writer out, String capitalizedKey, String methodPrefix, SelectionEnum value, String args, String[][] assignments) throws IOException {
        out.append(METHOD_START).append(self).append(' ').append(methodPrefix).append(capitalizedKey).append("(").append(args).append(") {\n");
        out.append("\t\t").append("this.").append(SELECT_PREFIX).append(capitalizedKey).append(" = ").append(selectionEnum.toString()).append(".").append(value.name()).append(";\n");
        if (assignments != null) {
            for (String[] assignment : assignments) {
                out.append("\t\t").append("this.").append(assignment[0]).append(" = ").append(assignment[1]).append(";\n");
            }
        }
        out.append("\t\t").append("return this;\n");
        out.append(METHOD_END);
    }

    private void appendOverride(Writer out) throws IOException {
        out.append("\t@java.lang.Override\n");
    }

    private void addJacksonTyping(SelectionMeta meta, Writer out) throws IOException {
        if (!meta.isAbstract()) {
            appendJsonSubType(meta, meta.getTargetPackage(), out);
        }
        for (SelectionMeta child : meta.getChildren()) {
            addJacksonTyping(child, out);
        }
    }

    private void appendJsonSubType(SelectionMeta meta, PackageElement targetPackage, Writer out) throws IOException {
        out.append("\t");
        out.append("@").append(jsonSubTypes.toString()).append(".Type(value = ");
        out.append(getQualifiedName(meta, targetPackage, CLASS_PREFIX));
        out.append(".class");
        out.append(", name = \"").append(meta.getJacksonTypeTag()).append("\"),\n");
    }

    @Override
    TypeMirror getInterfaceRaw() {
        return selectionInterface;
    }

}
