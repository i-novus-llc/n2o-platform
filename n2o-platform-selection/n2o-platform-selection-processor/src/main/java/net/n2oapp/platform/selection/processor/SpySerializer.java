package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.SelectionPropagation;
import net.n2oapp.platform.selection.api.Spy;

import java.io.IOException;
import java.io.Writer;

@SuppressWarnings("java:S1192")
public class SpySerializer extends AbstractSerializer {

    @Override
    void preSerialize(final SelectionMeta meta, final String self, final Writer out) throws IOException {
        if (meta.getParent() == null) {
            out.append("\tprotected final ");
            out.append(meta.getModelType());
            out.append(" model;\n");
            out.append("\tprotected final ");
            out.append(meta.getSelectionType());
            out.append(" selection;\n");
            out.append("\tprotected final ");
            out.append(SelectionPropagation.class.getCanonicalName());
            out.append(" propagation;\n");
            appendNestedSpies(meta, out);
            appendConstructorDeclaration(meta, out);
            out.append("\t\tif (model == null) throw new java.lang.NullPointerException();\n");
            out.append("\t\tthis.model = model;\n");
            out.append("\t\tthis.selection = selection;\n");
            out.append("\t\tthis.propagation = propagation == null ? ");
            out.append(SelectionPropagation.class.getCanonicalName());
            out.append(".");
            out.append(SelectionPropagation.NORMAL.name());
            out.append(" : propagation;\n");
            appendNestedSpiesInitialization(meta, out);
            out.append("\t}\n\n");
            appendOverride(out);
            out.append("\tpublic ").append(meta.getSelectionType()).append(" getSelection() {\n");
            out.append("\t\treturn selection;\n");
            out.append("\t}\n\n");
            appendOverride(out);
            out.append("\tpublic ").append(meta.getModelType()).append(" getModel() {\n");
            out.append("\t\treturn model;\n");
            out.append("\t}\n\n");
        } else {
            appendNestedSpies(meta, out);
            appendConstructorDeclaration(meta, out);
            out.append("\t\tsuper(model, selection, propagation);\n");
            appendNestedSpiesInitialization(meta, out);
            out.append("\t}\n\n");
        }
        if (!meta.isAbstract() && meta.getUnresolvedProperties().isEmpty() && meta.getSelectionGenericSignature().noGenericsDeclared()) {
            out.append("\tpublic static ");
            out.append(meta.getSpyGenericSignature().toString());
            out.append(" ");
            out.append(self);
            out.append(" spy(");
            out.append(meta.getModelType());
            out.append(" model, ");
            out.append(meta.getSelectionType());
            out.append(" selection) {\n");
            out.append("\t\tif (selection == null || selection.empty()) return null;\n");
            out.append("\t\treturn new ").append(self).append("(model, selection, selection.getPropagation());\n");
            out.append("\t}\n");
        }
    }

    private void appendNestedSpiesInitialization(final SelectionMeta meta, final Writer out) throws IOException {
        for (final SelectionProperty property : meta.getProperties()) {
            String capitalized = capitalize(property.getName());
            if (property.selective()) {
                String nested = getQualifiedName(property.getSelection());
                String generics = property.getSelection().getSelectionTypeVariable() == null ? property.getGenerics() : property.getGenerics("?");
                out.append("\t\tif (model.get");
                out.append(capitalized);
                out.append("() != null) {\n");
                nestedSelectionPredicate(out, "\t", capitalized);
                if (property.getCollectionType() == null) {
                    out.append("\t\t\t\tthis.");
                    out.append(property.getName());
                    out.append(" = ");
                    nestedSpy(out, capitalized, nested, generics, "model.get" + capitalized + "()");
                    out.append(";\n");
                } else {
                    out.append("\t\t\t\tthis.").append(property.getName()).append(" = new ");
                    if (property.getCollectionType().toString().equals("java.util.List")) {
                        out.append("java.util.ArrayList<>(model.get").append(capitalized).append("().size());\n");
                    } else
                        out.append("java.util.HashSet<>(model.get").append(capitalized).append("().size());\n");
                    out.append("\t\t\t\tfor (").append(property.getTypeStr()).append(" nested : model.get").append(capitalized).append("()) {\n");
                    out.append("\t\t\t\t\tthis.").append(property.getName()).append(".add(");
                    nestedSpy(out, capitalized, nested, generics, "nested");
                    out.append(");\n");
                    out.append("\t\t\t\t}\n");
                }
                out.append("\t\t\t} else this.").append(property.getName()).append(" = null;\n");
                out.append("\t\t} else this.").append(property.getName()).append(" = null;\n");
            }
        }
    }

    private void nestedSpy(
        final Writer out,
        final String capitalized,
        final String nested,
        final String generics,
        final String nestedSpyRef
    ) throws IOException {
        out.append("new ");
        out.append(nested);
        if (!generics.isBlank()) {
            out.append("<>");
        }
        out.append("(");
        out.append(nestedSpyRef);
        out.append(", selection == null ? null : selection.get");
        out.append(capitalized);
        out.append("(), propagation == ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(".");
        out.append(SelectionPropagation.NESTED.name());
        out.append(" ? propagation : selection.get");
        out.append(capitalized);
        out.append("().getPropagation())");
    }

    private void nestedSelectionPredicate(
        Writer out,
        String tabs,
        String capitalizedProperty
    ) throws IOException {
        out.append(tabs).append("\t\tif (propagation == ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(".");
        out.append(SelectionPropagation.NESTED.name());
        out.append(" || (selection.get");
        out.append(capitalizedProperty);
        out.append("() != null && !selection.get");
        out.append(capitalizedProperty);
        out.append("().empty())) {\n");
    }

    private void appendNestedSpies(final SelectionMeta meta, final Writer out) throws IOException {
        for (final SelectionProperty property : meta.getProperties()) {
            if (property.selective()) {
                String nested = getQualifiedName(property.getSelection());
                String generics = property.getSelection().getSelectionTypeVariable() == null ? property.getGenerics() : property.getGenerics("?");
                if (property.getCollectionType() != null) {
                    out.append("\tprivate final ");
                    out.append(property.getCollectionType().toString());
                    out.append("<");
                    out.append(nested);
                    out.append(generics);
                    out.append("> ");
                    out.append(property.getName());
                    out.append(";\n");
                } else {
                    out.append("\tprivate final ");
                    out.append(nested);
                    out.append(generics);
                    out.append(" ");
                    out.append(property.getName());
                    out.append(";\n");
                }
            }
        }
    }

    private void appendConstructorDeclaration(final SelectionMeta meta, final Writer out) throws IOException {
        out.append("\n");
        out.append("\tpublic ");
        out.append(meta.getTarget().getSimpleName()).append(getSuffix());
        out.append("(");
        out.append(meta.getModelType());
        out.append(" model, ");
        out.append(meta.getSelectionType());
        out.append(" selection, ").append(SelectionPropagation.class.getCanonicalName()).append(" propagation) {\n");
    }

    @Override
    GenericSignature getGenericSignature(final SelectionMeta meta) {
        return meta.getSpyGenericSignature();
    }

    @Override
    String getExtendsSignature(final SelectionMeta meta) {
        return meta.getSpyExtendsSignature();
    }

    @Override
    void serializeProperty(final SelectionMeta meta, final String self, final SelectionProperty property, final Writer out) throws IOException {
        String capitalized = capitalize(property.getName());
        if (!property.selective()) {
            out.append("\tpublic ");
            out.append(property.getOriginalTypeStr());
            out.append(" get");
            out.append(capitalized);
            out.append("() {\n");
            out.append("\t\tif ((propagation == ");
            out.append(SelectionPropagation.class.getCanonicalName());
            out.append(".");
            out.append(SelectionPropagation.NESTED.name());
            out.append(" || propagation == ");
            out.append(SelectionPropagation.class.getCanonicalName());
            out.append(".");
            out.append(SelectionPropagation.ALL.name());
            out.append(") ");
            out.append(" || (selection.get");
            out.append(capitalized);
            out.append("() != null && selection.get");
            out.append(capitalized);
            out.append("().asBoolean())) {\n");
            out.append("\t\t\treturn model.get");
            out.append(capitalized);
            out.append("();\n");
            out.append("\t\t}\n");
            appendThrow(property, out);
            out.append("\t}\n");
        } else {
            out.append("\tpublic ");
            String nested = getQualifiedName(property.getSelection());
            String generics = property.getSelection().getSelectionTypeVariable() == null ? property.getGenerics() : property.getGenerics("?");
            if (property.getCollectionType() != null) {
                out.append(property.getCollectionType().toString());
                out.append("<");
                out.append(nested);
                out.append(generics);
                out.append(">");
            } else {
                out.append(nested);
                out.append(generics);
            }
            out.append(" get");
            out.append(capitalized);
            out.append("() {\n");
            nestedSelectionPredicate(out, "", capitalized);
            out.append("\t\t\treturn ");
            out.append(property.getName());
            out.append(";\n");
            out.append("\t\t}\n");
            appendThrow(property, out);
            out.append("\t}\n");
        }
    }

    private void appendThrow(final SelectionProperty property, final Writer out) throws IOException {
        out.append("\t\tthrow new java.lang.IllegalStateException(\"Property '");
        out.append(property.getName());
        out.append("' was not initialized\");\n");
    }

    @Override
    String getExtendsOrImplements(final SelectionMeta meta) {
        return meta.getParent() == null ? "implements" : "extends";
    }

    @Override
    String getClassOrInterface(final SelectionMeta meta) {
        return "class";
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Spy.class;
    }

}
