package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionEnum;
import net.n2oapp.platform.selection.api.SelectionPropagationEnum;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class SelectionSerializer extends AbstractSerializer {

    private static final String CLASS_PREFIX = "Default";
    private static final String UNSELECT_PREFIX = "unselect";
    private static final String METHOD_START = "\tpublic ";
    private static final String METHOD_END = "\t}\n\n";
    private static final String SELECTION_ARG = "selection";
    private static final String FIELD_START = "\tprivate ";
    private static final String FIELD_END = ";\n\n";
    private static final String NO_ARG_METHOD_BODY_START = "() {\n";
    private static final String METHOD_BODY_STATEMENT_START = "\t\t";
    private static final String RETURN_STATEMENT = "return ";
    private static final String NO_ARG_METHOD_CALL = "();\n";
    private static final String THIS = "this";
    private static final String WITH_ARG_METHOD_BODY_START = ") {\n";
    private static final String ASSIGNMENT = " = ";
    private static final String STATEMENT_END = ";\n";
    private static final String PROPAGATION = "propagation";
    private static final String PROPAGATION_CAPITALIZED = "Propagation";
    private static final String GETTER = "get";
    private static final String SETTER = "set";
    private static final String VOID = "void";

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
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append(GETTER);
        String capitalizedProperty = capitalize(property.getKey());
        out.append(capitalizedProperty);
        out.append("();");
        if (property.getNestedSelection() != null) {
            out.append("\n\t");
            appendSelectionKey(out, property.getKey());
            out.append("\n\t");
            SelectionMeta nestedSelection = property.getNestedSelection();
            out.append(getQualifiedName(nestedSelection, nestedSelection.getTargetPackage()));
            out.append(property.getNestedGenericSignatureOrWildcards());
            out.append(' ');
            out.append(GETTER);
            out.append(capitalizedProperty);
            out.append(getSuffix());
            out.append("();");
        }
    }

    @Override
    void postSerialize(SelectionMeta meta, Writer out) throws IOException {
        if (!meta.isAbstract()) {
            String selfDefault = getQualifiedName(meta, meta.getTargetPackage(), CLASS_PREFIX);
            out.append('\n');
            out.append(METHOD_START);
            out.append("static ");
            out.append(meta.getGenericSignature().toString());
            out.append(' ');
            out.append(selfDefault);
            out.append(meta.getGenericSignature().varsToString(true));
            out.append(' ');
            out.append("create");
            out.append(NO_ARG_METHOD_BODY_START);
            out.append(METHOD_BODY_STATEMENT_START);
            out.append(RETURN_STATEMENT);
            out.append("new ");
            out.append(selfDefault);
            if (!meta.getGenericSignature().isEmpty())
                out.append("<>");
            out.append(NO_ARG_METHOD_CALL);
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
            initClassFile(meta, targetPackage, className, out);
            if (meta.getParent() == null) {
                definePropagationField(meta, out);
            }
            writePropagationMethod(meta.getParent() != null, self, out);
            String prefix = meta.getPrefix();
            for (SelectionProperty property : meta.getProperties()) {
                String capitalizedKey = capitalize(property.getKey());
                fieldForSelectionEnum(out, prefix, property.getKey());
                getterForSelectionEnum(out, property.getKey());
                setterForSelectionEnum(out, property.getKey());
                if (property.getNestedSelection() != null) {
                    String nestedQualified = getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage(), CLASS_PREFIX);
                    fieldForNestedSelection(out, property, nestedQualified);
                    getterForNestedSelection(out, property, capitalizedKey, nestedQualified);
                    setterForNestedSelection(out, property, capitalizedKey, nestedQualified);
                }
                String nestedSelectionArg = "";
                String[][] assignments = null;
                if (property.getNestedSelection() != null) {
                    assignments = new String[1][2];
                    assignments[0][0] = property.getKey() + getSuffix();
                    nestedSelectionArg = getNestedAsMethodArg(property);
                    assignments[0][1] = SELECTION_ARG;
                }
                appendSelect(self, out, property.getKey(), "", SelectionEnum.T, nestedSelectionArg, assignments);
                if (assignments != null)
                    assignments[0][1] = "null";
                appendSelect(self, out, property.getKey(), UNSELECT_PREFIX, SelectionEnum.F, "", assignments);
            }
            if (overrideSelectionKeys)
                overrideSelectionKeys(meta, out, self);
            overrideIsEmptyMethod(meta, out);
            out.append("\n}");
        }
    }

    private void initClassFile(SelectionMeta meta, PackageElement targetPackage, String className, Writer out) throws IOException {
        appendPackage(targetPackage, out);
        if (addJacksonTyping && !meta.getChildren().isEmpty()) {
            out.append("@");
            out.append(jsonTypeInfo.toString());
            out.append("(use");
            out.append(ASSIGNMENT);
            out.append(jsonTypeInfo.toString());
            out.append(".Id.NAME, property");
            out.append(ASSIGNMENT);
            out.append("\"t\")\n");
            out.append("@");
            out.append(jsonSubTypes.toString());
            out.append("({\n");
            addJacksonTyping(meta, out);
            out.append("})\n");
        }
        out.append("public");
        if (meta.isAbstract())
            out.append(" abstract");
        out.append(" class ");
        out.append(className);
        out.append(meta.getGenericSignature().toString());
        if (meta.getParent() != null) {
            out.append(" extends ");
            out.append(getQualifiedName(meta.getParent(), meta.getParent().getTargetPackage(), CLASS_PREFIX));
            out.append(meta.getExtendsSignature());
        }
        out.append(" implements ");
        out.append(getQualifiedName(meta, targetPackage));
        out.append(meta.getGenericSignature().varsToString(true));
        out.append("{\n\n");
    }

    private void definePropagationField(SelectionMeta meta, Writer out) throws IOException {
        if (addJaxRsAnnotations) {
            out.append("\t@");
            out.append(requestParam.toString());
            out.append("(\"");
            out.append(meta.getPrefixOrGenerate());
            out.append(PROPAGATION_CAPITALIZED);
            out.append("\")\n");
        }
        out.append("\tprotected ");
        out.append(SelectionPropagationEnum.class.getCanonicalName());
        out.append(' ');
        out.append(PROPAGATION);
        out.append(FIELD_END);
        out.append(METHOD_START);
        out.append(SelectionPropagationEnum.class.getCanonicalName());
        out.append(' ');
        out.append(GETTER);
        out.append(PROPAGATION_CAPITALIZED);
        out.append(NO_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(PROPAGATION);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
        out.append(METHOD_START);
        out.append(VOID);
        out.append(' ');
        out.append(SETTER);
        out.append(PROPAGATION_CAPITALIZED);
        out.append('(');
        out.append(SelectionPropagationEnum.class.getCanonicalName());
        out.append(' ');
        out.append(PROPAGATION);
        out.append(WITH_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(THIS);
        out.append('.');
        out.append(PROPAGATION);
        out.append(ASSIGNMENT);
        out.append(PROPAGATION);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
        appendOverride(out);
        out.append(METHOD_START);
        out.append(SelectionPropagationEnum.class.getCanonicalName());
        out.append(' ');
        out.append(PROPAGATION);
        out.append(NO_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(PROPAGATION);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    private void overrideIsEmptyMethod(SelectionMeta meta, Writer out) throws IOException {
        appendOverride(out);
        out.append(METHOD_START);
        out.append("boolean");
        out.append(" empty");
        out.append(NO_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(" ");
        if (meta.getParent() == null) {
            out.append("(");
            out.append(PROPAGATION);
            out.append("()");
            out.append(" == ");
            out.append("null");
            out.append(" || ");
            out.append(PROPAGATION);
            out.append("()");
            out.append(" == ");
            out.append(SelectionPropagationEnum.class.getCanonicalName());
            out.append(".");
            out.append(SelectionPropagationEnum.NORMAL.name());
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
                out.append(property.getKey());
                out.append(" == ");
                out.append("null");
                out.append(" || ");
                out.append(property.getKey());
                out.append(" == ");
                out.append(SelectionEnum.class.getCanonicalName());
                out.append(".");
                out.append(SelectionEnum.F.name());
                if (property.getNestedSelection() != null) {
                    out.append(" || ");
                    out.append(property.getKey());
                    out.append(getSuffix());
                    out.append(" == ");
                    out.append("null");
                    out.append(" || ");
                    out.append(property.getKey());
                    out.append(getSuffix());
                    out.append(".");
                    out.append("empty()");
                }
                out.append(")");
            }
        }
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    private void writePropagationMethod(boolean override, String self, Writer out) throws IOException {
        if (override)
            appendOverride(out);
        out.append(METHOD_START);
        out.append(self);
        out.append(" propagate(");
        out.append(SelectionPropagationEnum.class.getCanonicalName());
        out.append(' ');
        out.append(PROPAGATION);
        out.append(WITH_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(THIS);
        out.append('.');
        out.append(PROPAGATION);
        out.append(ASSIGNMENT);
        out.append(PROPAGATION);
        out.append(STATEMENT_END);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(THIS);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    @Override
    protected GenericSignature getGenericSignature(SelectionMeta meta) {
        return meta.getGenericSignature();
    }

    @Override
    protected String getExtendsSignature(SelectionMeta meta) {
        return meta.getExtendsSignature();
    }

    private void fieldForSelectionEnum(Writer out, String prefix, String key) throws IOException {
        if (addJaxRsAnnotations) {
            out.append("\t@");
            out.append(requestParam.toString());
            out.append("(\"");
            out.append(prefix);
            out.append(prefix.isBlank() ? key : capitalize(key));
            out.append("\")\n");
        }
        out.append(FIELD_START);
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append(key);
        out.append(FIELD_END);
    }

    private void getterForSelectionEnum(Writer out, String key) throws IOException {
        appendOverride(out);
        out.append(METHOD_START);
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append(GETTER);
        out.append(capitalize(key));
        out.append(NO_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(key);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    private void setterForSelectionEnum(Writer out, String key) throws IOException {
        out.append(METHOD_START);
        out.append(VOID);
        out.append(' ');
        out.append(SETTER);
        out.append(capitalize(key));
        out.append("(");
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(' ');
        out.append(key);
        out.append(WITH_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(THIS);
        out.append('.');
        out.append(key);
        out.append(ASSIGNMENT);
        out.append(key);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    private void fieldForNestedSelection(Writer out, SelectionProperty property, String nestedQualified) throws IOException {
        if (addJaxRsAnnotations) {
            out.append("\t@");
            out.append(beanParam.toString());
            out.append("\n");
        }
        out.append(FIELD_START);
        out.append(nestedQualified);
        out.append(property.getNestedGenericSignatureOrWildcards());
        out.append(' ');
        out.append(property.getKey());
        out.append(getSuffix());
        out.append(FIELD_END);
    }

    private void getterForNestedSelection(Writer out, SelectionProperty property, String capitalizedKey, String nestedQualified) throws IOException {
        appendOverride(out);
        out.append(METHOD_START);
        out.append(nestedQualified);
        out.append(property.getNestedGenericSignatureOrWildcards());
        out.append(' ');
        out.append(GETTER);
        out.append(capitalizedKey);
        out.append(getSuffix());
        out.append(NO_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(property.getKey());
        out.append(getSuffix());
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    private void setterForNestedSelection(Writer out, SelectionProperty property, String capitalizedKey, String nestedQualified) throws IOException {
        out.append(METHOD_START);
        out.append(VOID);
        out.append(' ');
        out.append(SETTER);
        out.append(capitalizedKey);
        out.append(getSuffix());
        out.append("(");
        out.append(nestedQualified);
        out.append(property.getNestedGenericSignatureOrWildcards());
        out.append(' ');
        out.append(SELECTION_ARG);
        out.append(WITH_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(THIS);
        out.append('.');
        out.append(property.getKey());
        out.append(getSuffix());
        out.append(ASSIGNMENT);
        out.append(SELECTION_ARG);
        out.append(STATEMENT_END);
        out.append(METHOD_END);
    }

    private String getNestedAsMethodArg(SelectionProperty property) {
        return getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage(), CLASS_PREFIX) + property.getNestedGenericSignatureOrWildcards() + " " + SELECTION_ARG;
    }

    private void overrideSelectionKeys(SelectionMeta meta, Writer out, String self) throws IOException {
        SelectionMeta curr = meta;
        SelectionMeta parent = meta.getParent();
        while (parent != null) {
            if (curr.isRawUse())
                break;
            for (SelectionProperty property : parent.getProperties()) {
                String nestedSelectionArg = "";
                if (property.getNestedSelection() != null) {
                    String bounds = property.resolveTypeVariables(meta);
                    if (!bounds.isEmpty())
                        bounds = "<" + bounds + ">";
                    nestedSelectionArg = getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage(), CLASS_PREFIX) + bounds + " " + SELECTION_ARG;
                }
                String capitalizedKey = capitalize(property.getKey());
                appendOverride(out);
                out.append(METHOD_START);
                out.append(self);
                out.append(' ').append(property.getKey());
                if (!nestedSelectionArg.isEmpty()) {
                    out.append("(");
                    out.append(nestedSelectionArg);
                    out.append(WITH_ARG_METHOD_BODY_START);
                } else
                    out.append(NO_ARG_METHOD_BODY_START);
                out.append(METHOD_BODY_STATEMENT_START);
                out.append(RETURN_STATEMENT);
                out.append("(");
                out.append(self);
                out.append(") ");
                out.append("super.");
                out.append(property.getKey());
                if (nestedSelectionArg.isEmpty())
                    out.append(NO_ARG_METHOD_CALL);
                else {
                    out.append("(");
                    out.append(SELECTION_ARG);
                    out.append(")");
                    out.append(STATEMENT_END);
                }
                out.append(METHOD_END);
                appendOverride(out);
                out.append(METHOD_START);
                out.append(self);
                out.append(' ');
                out.append(UNSELECT_PREFIX);
                out.append(capitalizedKey);
                out.append(NO_ARG_METHOD_BODY_START);
                out.append(METHOD_BODY_STATEMENT_START);
                out.append(RETURN_STATEMENT);
                out.append("(");
                out.append(self);
                out.append(") ");
                out.append("super.");
                out.append(UNSELECT_PREFIX);
                out.append(capitalizedKey);
                out.append(NO_ARG_METHOD_CALL);
                out.append(METHOD_END);
            }
            curr = parent;
            parent = parent.getParent();
        }
    }

    private void appendSelect(String self, Writer out, String key, String methodPrefix, SelectionEnum value, String args, String[][] assignments) throws IOException {
        out.append(METHOD_START);
        out.append(self);
        out.append(' ');
        if (methodPrefix.isEmpty())
            out.append(key);
        else {
            out.append(methodPrefix);
            out.append(capitalize(key));
        }
        out.append("(");
        out.append(args);
        out.append(WITH_ARG_METHOD_BODY_START);
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(THIS);
        out.append('.');
        out.append(key);
        out.append(ASSIGNMENT);
        out.append(SelectionEnum.class.getCanonicalName());
        out.append(".");
        out.append(value.name());
        out.append(STATEMENT_END);
        if (assignments != null) {
            for (String[] assignment : assignments) {
                out.append(METHOD_BODY_STATEMENT_START);
                out.append(THIS);
                out.append('.');
                out.append(assignment[0]);
                out.append(ASSIGNMENT);
                out.append(assignment[1]);
                out.append(STATEMENT_END);
            }
        }
        out.append(METHOD_BODY_STATEMENT_START);
        out.append(RETURN_STATEMENT);
        out.append(THIS);
        out.append(STATEMENT_END);
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
        out.append("@");
        out.append(jsonSubTypes.toString());
        out.append(".Type(value");
        out.append(ASSIGNMENT);
        out.append(getQualifiedName(meta, targetPackage, CLASS_PREFIX));
        out.append(".class");
        out.append(", name");
        out.append(ASSIGNMENT);
        out.append("\"");
        out.append(meta.getJacksonTypeTag());
        out.append("\"),\n");
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Selection.class;
    }

}
