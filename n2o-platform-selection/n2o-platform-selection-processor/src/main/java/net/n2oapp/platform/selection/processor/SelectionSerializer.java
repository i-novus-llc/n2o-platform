package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionEnum;
import net.n2oapp.platform.selection.api.SelectionPropagation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;

@SuppressWarnings("java:S1192")
class SelectionSerializer extends AbstractSerializer {

    private final TypeMirror jsonTypeInfo;
    private final TypeMirror jsonSubTypes;
    private final TypeElement requestParam;
    private final TypeElement beanParam;

    private final boolean addJacksonTyping;
    private final boolean addJaxRsAnnotations;
    private final boolean overrideSelectionKeys;

    SelectionSerializer(
            boolean addJacksonTyping,
            boolean addJaxRsAnnotations,
            boolean overrideSelectionKeys,
            TypeElement jsonTypeInfo,
            TypeElement jsonSubTypes,
            TypeElement requestParam,
            TypeElement beanParam
    ) {
        this.addJacksonTyping = addJacksonTyping;
        this.addJaxRsAnnotations = addJaxRsAnnotations;
        this.overrideSelectionKeys = overrideSelectionKeys;
        this.requestParam = requestParam;
        this.beanParam = beanParam;
        if (jsonTypeInfo != null) {
            this.jsonTypeInfo = jsonTypeInfo.asType();
            this.jsonSubTypes = jsonSubTypes.asType();
        } else {
            this.jsonTypeInfo = null;
            this.jsonSubTypes = null;
        }
    }

    @Override
    void serializeProperty(SelectionMeta meta, String self, SelectionProperty property, Writer out) throws IOException {
        String capitalizedKey = capitalize(property.getName());
        if (property.selective()) {
            String nestedSelection = getQualifiedName(property.getSelection());
            fieldForNestedSelection(out, property, nestedSelection);
            getterForNestedSelection(out, property, capitalizedKey, nestedSelection);
            setterForNestedSelection(out, property, capitalizedKey, nestedSelection);
        } else {
            fieldForSelectionEnum(out, property.getName());
        }
        String nestedSelectionArg = "";
        if (property.selective()) {
            nestedSelectionArg = getNestedAsMethodArg(property);
        }
        appendSelect(self, out, property.getName(), "", nestedSelectionArg.isEmpty() ? SelectionEnum.T : null, nestedSelectionArg);
        appendSelect(self, out, property.getName(), "unselect", nestedSelectionArg.isEmpty() ? SelectionEnum.F : null, "");
    }

    @Override
    void preSerialize(SelectionMeta meta, String self, Writer out) throws IOException {
        if (meta.getParent() == null) {
            definePropagationField(out);
        }
        writePropagationMethod(meta.getParent() != null, self, out);
        if (!meta.isAbstract() && meta.getUnresolvedProperties().isEmpty() && meta.getSelectionGenericSignature().noGenericsDeclared()) {
            out.append("\tpublic ");
            out.append("static ");
            out.append(meta.getSelectionGenericSignature().toString());
            out.append(' ');
            out.append(self);
            out.append(' ');
            out.append("create");
            out.append("() {\n");
            out.append("\t\t");
            out.append("return ");
            out.append("new ");
            out.append(self);
            out.append("();\n");
            out.append("\t}\n\n");
        }
        appendSelectionEnumAccessors(out, meta);
        if (overrideSelectionKeys)
            overrideSelectionKeys(meta, out, self);
        overrideIsEmptyMethod(meta, out);
        overrideCopyMethod(meta, self, out);
    }

    private void overrideCopyMethod(
        final SelectionMeta meta,
        final String self,
        final Writer out
    ) throws IOException {
        if (meta.getParent() != null) {
            appendOverride(out);
        }
        out.append("\tpublic ").append(self).append(" copy(").append(Selection.class.getCanonicalName()).append(" selection) {\n");
        out.append("\t\tif (selection instanceof ").append(getQualifiedName(meta)).append(") {\n");
        if (meta.getParent() == null) {
            out.append("\t\t\tthis.propagation = selection.propagation();\n");
        }
        out.append("\t\t\t").append(getQualifiedName(meta)).append(" source = (").append(getQualifiedName(meta)).append(") selection;\n");
        for (final SelectionProperty property : meta.getProperties()) {
            out.append("\t\t\tthis.").append(property.getName()).append(" = ").append("source.").append(property.getName()).append(";\n");
        }
        out.append("\t\t}\n");
        if (meta.getParent() != null)
            out.append("\t\tsuper.copy(selection);\n");
        out.append("\t\treturn this;\n");
        out.append("\t}\n\n");
    }

    private void appendSelectionEnumAccessors(Writer out, SelectionMeta meta) throws IOException {
        SelectionMeta curr = meta;
        do {
            for (SelectionProperty property : curr.getProperties()) {
                if (!property.selective()) {
                    getterForSelectionEnum(out, property.getName(), meta.getPrefix());
                    setterForSelectionEnum(out, property.getName(), meta.getPrefix());
                }
            }
            curr = curr.getParent();
        } while (curr != null);
        getterForPropagation(out, meta);
        setterForPropagation(out, meta);
    }

    private void definePropagationField(Writer out) throws IOException {
        propagationField(out);
        propagationMethod(out);
    }

    private void propagationField(Writer out) throws IOException {
        out.append("\tprotected ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(' ');
        out.append("propagation");
        out.append(";\n\n");
    }

    private void propagationMethod(Writer out) throws IOException {
        appendOverride(out);
        out.append("\tpublic ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(' ');
        out.append("propagation");
        out.append("() {\n");
        out.append("\t\t");
        out.append("return ");
        out.append("propagation;\n");
        out.append("\t}\n\n");
    }

    private void setterForPropagation(Writer out, SelectionMeta meta) throws IOException {
        appendQueryParam(out, "propagation", meta.getPrefixOrGenerate());
        out.append("\tpublic ");
        out.append("void");
        out.append(' ');
        out.append("setPropagation");
        out.append('(');
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(' ');
        out.append("propagation");
        out.append(") {\n");
        out.append("\t\t");
        out.append("this");
        out.append('.');
        out.append("propagation");
        out.append(" = ");
        out.append("propagation");
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void getterForPropagation(Writer out, SelectionMeta meta) throws IOException {
        appendQueryParam(out, "propagation", meta.getPrefixOrGenerate());
        out.append("\tpublic ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(' ');
        out.append("get");
        out.append("Propagation");
        out.append("() {\n");
        out.append("\t\t");
        out.append("return ");
        out.append("propagation");
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void overrideIsEmptyMethod(SelectionMeta meta, Writer out) throws IOException {
        appendOverride(out);
        out.append("\tpublic ");
        out.append("boolean");
        out.append(" empty");
        out.append("() {\n");
        out.append("\t\t");
        out.append("return ");
        out.append(" ");
        if (meta.getParent() == null) {
            out.append("(");
            out.append("propagation");
            out.append("()");
            out.append(" == ");
            out.append("null");
            out.append(" || ");
            out.append("propagation");
            out.append("()");
            out.append(" == ");
            out.append(SelectionPropagation.class.getCanonicalName());
            out.append(".");
            out.append(SelectionPropagation.NORMAL.name());
            out.append(")");
        } else {
            out.append("super.empty()");
        }
        if (!meta.getProperties().isEmpty()) {
            for (SelectionProperty property : meta.getProperties()) {
                out.append(" && ");
                out.append("\n");
                out.append("\t\t\t\t");
                out.append("(");
                if (property.selective()) {
                    out.append(property.getName());
                    out.append(" == ");
                    out.append("null");
                    out.append(" || ");
                    out.append(property.getName());
                    out.append(".");
                    out.append("empty()");
                } else {
                    out.append(property.getName());
                    out.append(" == ");
                    out.append("null");
                    out.append(" || ");
                    out.append(property.getName());
                    out.append(" == ");
                    out.append(SelectionEnum.class.getCanonicalName());
                    out.append(".");
                    out.append(SelectionEnum.F.name());
                }
                out.append(")");
            }
        }
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void writePropagationMethod(boolean override, String self, Writer out) throws IOException {
        if (override)
            appendOverride(out);
        out.append("\tpublic ");
        out.append(self);
        out.append(" propagate(");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(' ');
        out.append("propagation");
        out.append(") {\n");
        out.append("\t\t");
        out.append("this");
        out.append('.');
        out.append("propagation");
        out.append(" = ");
        out.append("propagation");
        out.append(";\n");
        out.append("\t\t");
        out.append("return ");
        out.append("this");
        out.append(";\n");
        out.append("\t}\n");
    }

    @Override
    GenericSignature getGenericSignature(SelectionMeta meta) {
        return meta.getSelectionGenericSignature();
    }

    @Override
    String getExtendsSignature(SelectionMeta meta) {
        return meta.getSelectionExtendsSignature();
    }

    private void fieldForSelectionEnum(Writer out, String key) throws IOException {
        out.append("\tprotected ");
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append(key);
        out.append(";\n\n");
    }

    private void getterForSelectionEnum(Writer out, String key, String prefix) throws IOException {
        appendQueryParam(out, key, prefix);
        out.append("\tpublic ");
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append("get");
        out.append(capitalize(key));
        out.append("() {\n");
        out.append("\t\t");
        out.append("return ");
        out.append(key);
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void setterForSelectionEnum(Writer out, String key, String prefix) throws IOException {
        appendQueryParam(out, key, prefix);
        out.append("\tpublic ");
        out.append("void");
        out.append(' ');
        out.append("set");
        out.append(capitalize(key));
        out.append("(");
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append(key);
        out.append(") {\n");
        out.append("\t\t");
        out.append("this");
        out.append('.');
        out.append(key);
        out.append(" = ");
        out.append(key);
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void appendQueryParam(Writer out, String key, String prefix) throws IOException {
        if (addJaxRsAnnotations) {
            out.append("\t@");
            out.append(requestParam.toString());
            out.append("(\"");
            out.append(prefix);
            out.append(prefix.isBlank() ? key : capitalize(key));
            out.append("\")\n");
        }
    }

    private void fieldForNestedSelection(Writer out, SelectionProperty property, String nestedQualified) throws IOException {
        if (addJaxRsAnnotations) {
            out.append("\t@");
            out.append(beanParam.toString());
            out.append("\n");
        }
        out.append("\tprotected ");
        out.append(nestedQualified);
        out.append(property.getGenerics());
        out.append(' ');
        out.append(property.getName());
        out.append(";\n\n");
    }

    private void getterForNestedSelection(Writer out, SelectionProperty property, String capitalizedKey, String nestedQualified) throws IOException {
        out.append("\tpublic ");
        out.append(nestedQualified);
        out.append(property.getGenerics());
        out.append(' ');
        out.append("get");
        out.append(capitalizedKey);
        out.append("() {\n");
        out.append("\t\t");
        out.append("return ");
        out.append(property.getName());
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void setterForNestedSelection(Writer out, SelectionProperty property, String capitalizedKey, String nestedQualified) throws IOException {
        out.append("\tpublic ");
        out.append("void");
        out.append(' ');
        out.append("set");
        out.append(capitalizedKey);
        out.append("(");
        out.append(nestedQualified);
        out.append(property.getGenerics());
        out.append(' ');
        out.append("selection");
        out.append(") {\n");
        out.append("\t\t");
        out.append("this");
        out.append('.');
        out.append(property.getName());
        out.append(" = ");
        out.append("selection");
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private String getNestedAsMethodArg(SelectionProperty property) {
        return getQualifiedName(property.getSelection()) + property.getGenerics() + " " + "selection";
    }

    private void overrideSelectionKeys(SelectionMeta meta, Writer out, String self) throws IOException {
        SelectionMeta parent = meta.getParent();
        while (parent != null) {
            for (SelectionProperty property : parent.getProperties()) {
                String nestedSelectionParam = "";
                if (property.selective()) {
                    String generics = property.getGenerics();
                    nestedSelectionParam = getQualifiedName(property.getSelection()) + generics + " " + "selection";
                }
                String capitalizedKey = capitalize(property.getName());
                appendOverride(out);
                out.append("\tpublic ");
                out.append(self);
                out.append(' ').append(property.getName());
                if (!nestedSelectionParam.isEmpty()) {
                    out.append("(");
                    out.append(nestedSelectionParam);
                    out.append(") {\n");
                } else
                    out.append("() {\n");
                out.append("\t\t");
                out.append("return ");
                out.append("(");
                out.append(self);
                out.append(") ");
                out.append("super.");
                out.append(property.getName());
                if (nestedSelectionParam.isEmpty())
                    out.append("();\n");
                else {
                    out.append("(");
                    out.append("selection");
                    out.append(")");
                    out.append(";\n");
                }
                out.append("\t}\n\n");
                appendOverride(out);
                out.append("\tpublic ");
                out.append(self);
                out.append(' ');
                out.append("unselect");
                out.append(capitalizedKey);
                out.append("() {\n");
                out.append("\t\t");
                out.append("return ");
                out.append("(");
                out.append(self);
                out.append(") ");
                out.append("super.");
                out.append("unselect");
                out.append(capitalizedKey);
                out.append("();\n");
                out.append("\t}\n\n");
            }
            parent = parent.getParent();
        }
    }

    private void appendSelect(String self, Writer out, String key, String methodPrefix, SelectionEnum value, String nestedSelectionArg) throws IOException {
        out.append("\tpublic ");
        out.append(self);
        out.append(' ');
        if (methodPrefix.isEmpty())
            out.append(key);
        else {
            out.append(methodPrefix);
            out.append(capitalize(key));
        }
        out.append("(");
        out.append(nestedSelectionArg);
        out.append(") {\n");
        out.append("\t\t");
        if (value != null) {
            out.append("this");
            out.append('.');
            out.append(key);
            out.append(" = ");
            out.append(SelectionEnum.class.getCanonicalName());
            out.append(".");
            out.append(value.name());
            out.append(";\n");
        } else {
            out.append("this");
            out.append('.');
            out.append(key);
            out.append(" = ");
            out.append(nestedSelectionArg.isEmpty() ? "null" : "selection");
            out.append(";\n");
        }
        out.append("\t\t");
        out.append("return ");
        out.append("this");
        out.append(";\n");
        out.append("\t}\n\n");
    }

    private void addJacksonTyping(SelectionMeta meta, Writer out) throws IOException {
        if (!meta.isAbstract()) {
            appendJsonSubType(meta, out);
        }
        for (SelectionMeta child : meta.getChildren()) {
            addJacksonTyping(child, out);
        }
    }

    private void appendJsonSubType(SelectionMeta meta, Writer out) throws IOException {
        out.append("\t");
        out.append("@");
        out.append(jsonSubTypes.toString());
        out.append(".Type(value");
        out.append(" = ");
        out.append(getQualifiedName(meta));
        out.append(".class");
        out.append(", name");
        out.append(" = ");
        out.append("\"");
        out.append(meta.getJacksonTypeTag());
        out.append("\"),\n");
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Selection.class;
    }

    @Override
    String getClassOrInterface(SelectionMeta meta) {
        return meta.isAbstract() ? "abstract class" : "class";
    }

    @Override
    String getExtendsOrImplements(SelectionMeta meta) {
        if (meta.getParent() == null)
            return "implements";
        return "extends";
    }

    @Override
    void appendTypeAnnotations(SelectionMeta meta, Writer out) throws IOException {
        if (addJacksonTyping && !meta.getChildren().isEmpty()) {
            out.append("@");
            out.append(jsonTypeInfo.toString());
            out.append("(use");
            out.append(" = ");
            out.append(jsonTypeInfo.toString());
            out.append(".Id.NAME, property");
            out.append(" = ");
            out.append("\"t\")\n");
            out.append("@");
            out.append(jsonSubTypes.toString());
            out.append("({\n");
            addJacksonTyping(meta, out);
            out.append("})\n");
        }
    }

}
