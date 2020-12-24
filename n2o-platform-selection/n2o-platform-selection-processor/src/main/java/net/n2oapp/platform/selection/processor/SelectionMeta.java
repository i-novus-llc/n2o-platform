package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import java.util.ArrayList;
import java.util.List;

class SelectionMeta {

    private final Element target;
    private final Element parent;
    private final boolean isAbstract;
    private final String className;
    private final List<String> imports;

    SelectionMeta(Element target, Element parent) {
        this.target = target;
        this.parent = parent;
        this.imports = new ArrayList<>();
        this.className = target.getSimpleName() + "Selection";
        this.imports.addAll(List.of(
                "net.n2oapp.platform.selection.api.Selection",
                "net.n2oapp.platform.selection.api.SelectionEnum",
                "net.n2oapp.platform.selection.api.SelectionKey"
        ));
        this.isAbstract = target.getModifiers().stream().anyMatch(Modifier.ABSTRACT::equals);
    }

    Name getTargetPackage() {
        return ((PackageElement) target.getEnclosingElement()).getQualifiedName();
    }

    List<String> getImports() {
        return imports;
    }

    String getClassName() {
        return className;
    }





}
