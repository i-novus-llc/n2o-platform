package net.n2oapp.platform.selection.processor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

abstract class AbstractSerializer {

    private final TypeMirror selectionKey;

    AbstractSerializer(TypeMirror selectionKey) {
        this.selectionKey = selectionKey;
    }

    abstract String getSuffix();
    abstract void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException;
    abstract TypeMirror getInterfaceRaw();

    void serialize(SelectionMeta meta, Filer filer) throws IOException {
        PackageElement targetPackage = meta.getTargetPackage();
        String interfaceName = meta.getTarget().getSimpleName() + getSuffix();
        JavaFileObject file = filer.createSourceFile(getQualifiedName(meta, targetPackage), targetPackage);
        try (Writer out = file.openWriter()) {
            appendPackage(targetPackage, out);
            out.append("public interface ").append(interfaceName).append(meta.getGenericSignature().toString());
            out.append(" extends ");
            if (meta.getParent() != null) {
                out.append(getQualifiedName(meta.getParent(), meta.getParent().getTargetPackage()));
                out.append(meta.getExtendsSignature());
            } else {
                out.append(getInterfaceRaw().toString()).append(meta.getExtendsSignature());
            }
            out.append(" {");
            for (SelectionProperty property : meta.getProperties()) {
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

    void postSerialize(SelectionMeta meta, Writer out) throws IOException {
    }

    protected Writer appendPackage(PackageElement targetPackage, Writer out) throws IOException {
        return out.append("package ").append(targetPackage.getQualifiedName()).append(';').append("\n\n");
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
        out.append("@").append(selectionKey.toString()).append("(\"").append(key).append("\")");
    }

}
