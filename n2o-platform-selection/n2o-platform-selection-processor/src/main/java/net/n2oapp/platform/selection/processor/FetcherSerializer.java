package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.api.SelectionPropagation;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;

import static net.n2oapp.platform.selection.api.SelectionPropagation.NESTED;

@SuppressWarnings("java:S1192")
class FetcherSerializer extends AbstractSerializer {

    @Override
    void serializeProperty(SelectionMeta meta, String self, SelectionProperty property, Writer out) throws IOException {
        if (!property.selective()) {
            out.append("\t");
            out.append(property.getOriginalTypeStr());
            out.append(" ");
            out.append("fetch");
            out.append(capitalize(property.getName()));
            out.append("();");
        } else {
            out.append("\t");
            if (property.getCollectionType() != null) {
                out.append(property.getCollectionType().toString());
                out.append("<");
            }
            out.append(getQualifiedName(property.getSelection()));
            SelectionMeta nestedSelective = property.getSelection();
            if (nestedSelective.getSelectionTypeVariable() == null) {
                out.append(property.getGenerics("?"));
            } else {
                String selection = getQualifiedName(nestedSelective, Selection.class.getSimpleName());
                String generics = property.getGenerics();
                selection += generics;
                out.append(property.getGenerics(selection, "?"));
            }
            if (property.getCollectionType() != null)
                out.append('>');
            out.append(' ');
            out.append("fetch");
            out.append(capitalize(property.getName()));
            out.append("();");
        }
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Fetcher.class;
    }

    @Override
    void preSerialize(SelectionMeta meta, String self, Writer out) throws IOException {
        appendOverride(out);
        out.append("\tdefault");
        out.append(" ");
        out.append(meta.getModelType());
        out.append(" ");
        out.append("resolve(final ");
        out.append(meta.getSelectionType());
        out.append(" ");
        out.append("selection, ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(" propagation");
        out.append(") {\n");
        if (meta.getParent() == null) {
            appendExplicitPropagation(out);
            appendReturnNullIfSelectionEmpty(out);
            out.append("\t\t");
            out.append(meta.getModelType());
            out.append(" model = create();\n");
        } else {
            out.append("\t\t");
            out.append(meta.getModelType());
            out.append(" model = ");
            out.append(getQualifiedName(meta.getParent()));
            out.append(".super.resolve(selection, propagation);\n");
            out.append("\t\tif (model == null) return null;\n");
            appendExplicitPropagation(out);
        }
        for (SelectionProperty property : meta.getProperties()) {
            appendProperty(out, property, "this", "");
        }
        out.append("\t\treturn model;\n");
        out.append("\t}");
    }

    static void appendProperty(Writer out, SelectionProperty property, String selfReference, String tabs) throws IOException {
        String capitalizedKey = capitalize(property.getName());
        String getter = "get" + capitalizedKey + "()";
        SelectionMeta nestedSelective = property.getSelection();
        out.append(tabs);
        out.append("\t\t");
        appendSelectionPredicate(out, property);
        out.append(" {\n");
        if (nestedSelective == null) {
            out.append(tabs);
            out.append("\t\t\tmodel.set");
            out.append(capitalizedKey);
            out.append("(");
            out.append(selfReference);
            out.append(".");
            out.append("fetch");
            out.append(capitalizedKey);
            out.append("());\n");
        } else {
            String nestedFetcher = getQualifiedName(nestedSelective, Fetcher.class.getSimpleName());
            if (nestedSelective.getSelectionTypeVariable() == null) {
                nestedFetcher += property.getGenerics("?");
            } else {
                String selection = getQualifiedName(nestedSelective, Selection.class.getSimpleName());
                String generics = property.getGenerics();
                selection += generics;
                nestedFetcher += property.getGenerics(selection, "?");
            }
            if (property.getCollectionType() == null) {
                out.append(tabs);
                out.append("\t\t\t");
                out.append(nestedFetcher);
                out.append(" nestedFetcher = ");
                appendFetch(out, selfReference, tabs, capitalizedKey);
                out.append("\t\t\tif (nestedFetcher != null) {\n");
                out.append(tabs);
                out.append("\t\t\t\t");
                out.append("model.set");
                out.append(capitalizedKey);
                out.append("(");
                appendResolveNestedFetcher(out, getter);
                out.append(");\n");
            } else {
                out.append(tabs);
                out.append("\t\t\t");
                out.append(property.getCollectionType().toString());
                out.append("<");
                out.append(nestedFetcher);
                out.append("> nestedFetchers = ");
                appendFetch(out, selfReference, tabs, capitalizedKey);
                out.append("\t\t\tif (nestedFetchers != null && !nestedFetchers.isEmpty()) {\n");
                out.append(tabs);
                out.append("\t\t\t\t");
                out.append(property.getCollectionType().toString());
                out.append("<");
                out.append(property.getTypeStr());
                out.append("> result = new ");
                if (property.getCollectionType().toString().equals("java.util.List")) {
                    out.append(ArrayList.class.getCanonicalName());
                    out.append("<>(nestedFetchers.size());\n");
                } else if (property.getCollectionType().toString().equals("java.util.Set")) {
                    out.append(HashSet.class.getCanonicalName());
                    out.append("<>(nestedFetchers.size());\n");
                }
                out.append(tabs);
                out.append("\t\t\t\tfor (");
                out.append(nestedFetcher);
                out.append(" nestedFetcher : nestedFetchers) {\n");
                out.append(tabs);
                out.append("\t\t\t\t\tresult.add(");
                appendResolveNestedFetcher(out, getter);
                out.append(");\n");
                out.append(tabs);
                out.append("\t\t\t\t}\n");
            }
            out.append(tabs);
            out.append("\t\t\t}\n");
        }
        out.append(tabs);
        out.append("\t\t}\n");
    }

    private static void appendFetch(final Writer out, final String selfReference, final String tabs, final String capitalizedKey) throws IOException {
        out.append(selfReference);
        out.append(".");
        out.append("fetch");
        out.append(capitalizedKey);
        out.append("();\n");
        out.append(tabs);
    }

    private static void appendResolveNestedFetcher(Writer out, String nestedSelectionGetter) throws IOException {
        out.append("nestedFetcher.resolve(selection == null ? null : selection.");
        out.append(nestedSelectionGetter);
        out.append(", propagation == ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(".");
        out.append(NESTED.name());
        out.append(" ? propagation : ");
        out.append("selection.");
        out.append(nestedSelectionGetter);
        out.append(".propagation())");
    }

    @Override
    GenericSignature getGenericSignature(SelectionMeta meta) {
        return meta.getFetcherGenericSignature();
    }

    @Override
    String getExtendsSignature(SelectionMeta meta) {
        return meta.getFetcherExtendsSignature();
    }

}
