package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

class SelectionMeta {

    private final TypeElement target;
    private final TypeElement parent;
    private final boolean isAbstract;
    private final List<String> imports;
    private final GenericSignature genericSignature;

    SelectionMeta(TypeElement target, TypeElement parent, GenericSignature genericSignature) {
        this.target = target;
        this.parent = parent;
        this.genericSignature = genericSignature;
        this.imports = new ArrayList<>();
        this.isAbstract = target.getModifiers().stream().anyMatch(Modifier.ABSTRACT::equals);
        if (isAbstract)
            genericSignature.createSelfVariable();
    }

    Name getTargetPackage() {
        return ((PackageElement) target.getEnclosingElement()).getQualifiedName();
    }

    List<String> getImports() {
        return imports;
    }

}
