package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.SelectionPropagation;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

import static net.n2oapp.platform.selection.api.SelectionPropagation.NORMAL;

abstract class AbstractSerializer {

    AbstractSerializer() {
    }

    String getSuffix() {
        return getInterfaceRaw().getSimpleName();
    }

    void serialize(SelectionMeta meta, Filer filer) throws IOException {
        final PackageElement targetPackage = meta.getTargetPackage();
        final String name = meta.getTarget().getSimpleName() + getSuffix();
        final String qualifiedName = getQualifiedName(meta);
        final String self = qualifiedName + getGenericSignature(meta).varsToString(true);
        JavaFileObject file = filer.createSourceFile(qualifiedName, targetPackage);
        try (Writer out = file.openWriter()) {
            appendPackage(targetPackage, out);
            appendTypeAnnotations(meta, out);
            out.append("public ");
            out.append(getClassOrInterface(meta));
            out.append(" ");
            out.append(name);
            out.append(getGenericSignature(meta).toString());
            out.append(" ");
            out.append(getExtendsOrImplements(meta));
            out.append(" ");
            if (meta.getParent() != null) {
                out.append(getQualifiedName(meta.getParent()));
            } else {
                out.append(getInterfaceRaw().getCanonicalName());
            }
            out.append(getExtendsSignature(meta));
            out.append(" {\n\n");
            preSerialize(meta, self, out);
            out.append('\n');
            for (SelectionProperty property : meta.getProperties()) {
                if (!shouldSerialize(property))
                    continue;
                out.append("\n");
                serializeProperty(meta, self, property, out);
            }
            out.append("\n\n");
            out.append("}");
        }
    }

    abstract void preSerialize(SelectionMeta meta, String self, Writer out) throws IOException;
    boolean shouldSerialize(SelectionProperty property) {
        return true;
    }
    abstract GenericSignature getGenericSignature(SelectionMeta meta);
    abstract String getExtendsSignature(SelectionMeta meta);
    abstract void serializeProperty(SelectionMeta meta, String self, SelectionProperty property, Writer out) throws IOException;
    abstract Class<?> getInterfaceRaw();
    abstract String getClassOrInterface(SelectionMeta meta);
    abstract String getExtendsOrImplements(SelectionMeta meta);
    abstract void appendTypeAnnotations(SelectionMeta meta, Writer out) throws IOException;

    void appendPackage(PackageElement targetPackage, Writer out) throws IOException {
        out.append("package ");
        out.append(targetPackage.getQualifiedName());
        out.append(';');
        out.append("\n\n");
    }

    String getQualifiedName(SelectionMeta meta) {
        return getQualifiedName(meta, getSuffix());
    }

    static String getQualifiedName(SelectionMeta meta, String suffix) {
        return meta.getTargetPackage() + "." + meta.getTarget().getSimpleName().toString() + suffix;
    }

    static String capitalize(String key) {
        if (key == null || key.length() == 0) {
            return key;
        }
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    void appendOverride(Writer out) throws IOException {
        out.append("\t@java.lang.Override\n");
    }

    void appendExplicitPropagation(Writer out) throws IOException {
        out.append("\t\tpropagation = propagation == null ? ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(".");
        out.append(NORMAL.name());
        out.append(" : propagation;\n");
    }

    void appendReturnNullIfSelectionEmpty(Writer out) throws IOException {
        out.append("\t\tif (propagation == ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(".");
        out.append(NORMAL.name());
        out.append(" && (selection == null || selection.empty())) return null;\n");
    }

    static void appendSelectionPredicate(Writer out, SelectionProperty property) throws IOException {
        out.append("if ((propagation == ");
        out.append(SelectionPropagation.class.getCanonicalName());
        out.append(".");
        out.append(SelectionPropagation.NESTED.name());
        if (!property.selective()) {
            out.append(" || propagation == ");
            out.append(SelectionPropagation.class.getCanonicalName());
            out.append(".");
            out.append(SelectionPropagation.ALL.name());
        }
        out.append(") || (selection != null && selection.get");
        out.append(capitalize(property.getName()));
        out.append("() != null && ");
        if (property.selective())
            out.append("!");
        out.append("selection.get");
        out.append(capitalize(property.getName()));
        out.append("()");
        if (property.selective()) {
            out.append(".empty()))");
        } else
            out.append(".asBoolean()))");
    }

}
