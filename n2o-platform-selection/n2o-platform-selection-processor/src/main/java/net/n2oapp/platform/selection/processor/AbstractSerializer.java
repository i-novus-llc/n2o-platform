package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.SelectionKey;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

abstract class AbstractSerializer {

    AbstractSerializer() {
    }

    String getSuffix() {
        return getInterfaceRaw().getSimpleName();
    }

    abstract void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException;
    abstract Class<?> getInterfaceRaw();

    void serialize(SelectionMeta meta, Filer filer) throws IOException {
        PackageElement targetPackage = meta.getTargetPackage();
        String interfaceName = meta.getTarget().getSimpleName() + getSuffix();
        JavaFileObject file = filer.createSourceFile(getQualifiedName(meta, targetPackage), targetPackage);
        try (Writer out = file.openWriter()) {
            appendPackage(targetPackage, out);
            out.append("public interface ");
            out.append(interfaceName);
            out.append(getGenericSignature(meta).toString());
            out.append(" extends ");
            if (meta.getParent() != null) {
                out.append(getQualifiedName(meta.getParent(), meta.getParent().getTargetPackage()));
            } else {
                out.append(getInterfaceRaw().getCanonicalName());
            }
            out.append(getExtendsSignature(meta));
            out.append(" {");
            for (SelectionProperty property : meta.getProperties()) {
                if (!shouldSerialize(property))
                    continue;
                out.append("\n\t");
                appendSelectionKey(out, property.getKey());
                out.append("\n\t");
                serializeProperty(meta, property, out);
            }
            out.append('\n');
            postSerialize(meta, out);
            out.append("}");
        }
    }

    protected boolean shouldSerialize(SelectionProperty property) {
        return true;
    }

    protected abstract GenericSignature getGenericSignature(SelectionMeta meta);
    protected abstract String getExtendsSignature(SelectionMeta meta);

    void postSerialize(SelectionMeta meta, Writer out) throws IOException {
    }

    protected void appendPackage(PackageElement targetPackage, Writer out) throws IOException {
        out.append("package ");
        out.append(targetPackage.getQualifiedName());
        out.append(';');
        out.append("\n\n");
    }

    String getQualifiedName(SelectionMeta meta, PackageElement targetPackage) {
        return getQualifiedName(meta, targetPackage, "");
    }

    String getQualifiedName(SelectionMeta meta, PackageElement targetPackage, String prefix) {
        return targetPackage + "." + prefix + meta.getTarget().getSimpleName().toString() + getSuffix();
    }

    String capitalize(String key) {
        if (key == null || key.length() == 0) {
            return key;
        }
        return key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    void appendSelectionKey(Writer out, String key) throws IOException {
        out.append("@");
        out.append(SelectionKey.class.getCanonicalName());
        out.append("(\"");
        out.append(key);
        out.append("\")");
    }

}
