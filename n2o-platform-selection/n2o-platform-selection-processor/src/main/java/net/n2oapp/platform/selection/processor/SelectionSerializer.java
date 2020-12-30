package net.n2oapp.platform.selection.processor;

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
                out.append('\t').append("private").append(' ').append(selectionPropagation.toString()).append(' ').append("propagation;\n\n");
                appendOverride(out);
                out.append("\tpublic ").append(selectionPropagation.toString()).append(' ').append("propagation() {\n");
                out.append("\t\t").append("return propagation;\n");
                out.append("\t}\n\n");
                out.append("\tpublic ").append(self).append(" propagate(").append(selectionPropagation.toString()).append(' ');
                out.append("propagation) {\n");
                out.append("\t\t");
                out.append("this.propagation = propagation;\n");
                out.append("\t\t");
                out.append("return this;\n");
                out.append("\t}\n\n");
            }
            for (SelectionProperty property : meta.getProperties()) {
                String key = capitalize(property.getKey());
                out.append("\t");
                out.append("private").append(' ').append(selectionEnum.toString()).append(' ').append("select").append(key).append(";\n\n");
                appendOverride(out);
                out.append("\t");
                out.append("public ").append(selectionEnum.toString()).append(' ').append(GETTER_PREFIX);
                out.append(key).append("() {\n");
                out.append("\t\t").append("return ").append("select").append(key).append(";\n");
                out.append('\t').append("}\n\n");
                
            }
            out.append("\n}");
        }
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
