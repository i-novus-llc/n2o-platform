package net.n2oapp.platform.selection.processor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

abstract class AbstractSerializer {

    abstract String getSuffix();

    void serialize(SelectionMeta meta, Filer filer) throws IOException {
        PackageElement targetPackage = meta.getTargetPackage();
        String name = targetPackage + "." + meta.getTarget().getSimpleName().toString() + getSuffix();
        String className = meta.getTarget().getSimpleName() + getSuffix();
        JavaFileObject file = filer.createSourceFile(name, targetPackage);
        try (Writer out = file.openWriter()) {
            out.append("package ").append(targetPackage.getQualifiedName()).append(';').append("\n\n");
            out.append("public interface ").append(className).append(meta.getGenericSignature().toString()).append(" {\n");
            out.append("}");
        }
    }

}
