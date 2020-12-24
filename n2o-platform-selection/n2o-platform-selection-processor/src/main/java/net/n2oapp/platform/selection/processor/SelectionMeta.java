package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

class SelectionMeta {

    private final TypeElement target;
    private final SelectionMeta parent;
    private final boolean isAbstract;
    private final List<String> imports;
    private final GenericSignature genericSignature;
    private final String extendsSignature;

    SelectionMeta(TypeElement target, SelectionMeta parent, GenericSignature genericSignature, Types types) {
        this.target = target;
        this.parent = parent;
        this.genericSignature = genericSignature;
        this.imports = new ArrayList<>();
        this.isAbstract = target.getModifiers().stream().anyMatch(Modifier.ABSTRACT::equals);
        if (isAbstract)
            genericSignature.createSelfVariable();
        this.extendsSignature = resolveExtendsSignature(types);
    }

    private String resolveExtendsSignature(Types types) {
        if (parent == null) {
            if (!isAbstract)
                return target.getQualifiedName().toString();
            else {
                return genericSignature.getSelfVariable();
            }
        } else {
            if (parent.genericSignature.isEmpty())
                return "";
            DeclaredType extendsType = getExtendsType(types);
            if (extendsType.getTypeArguments().isEmpty())
                return "";
            System.out.println(1);
        }
        return null;
    }

    private DeclaredType getExtendsType(Types types) {
        TypeMirror parentType = null;
        TypeMirror parentErasure = types.erasure(parent.target.asType());
        for (TypeMirror mirror : types.directSupertypes(target.asType())) {
            if (types.isSameType(types.erasure(mirror), parentErasure)) {
                parentType = mirror;
                break;
            }
        }
        return (DeclaredType) parentType;
    }

    Name getTargetPackage() {
        return ((PackageElement) target.getEnclosingElement()).getQualifiedName();
    }

    List<String> getImports() {
        return imports;
    }

    TypeElement getTarget() {
        return target;
    }

}
