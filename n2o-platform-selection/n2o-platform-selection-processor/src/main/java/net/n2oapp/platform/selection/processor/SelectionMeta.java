package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

class SelectionMeta {

    private final TypeElement target;
    private final SelectionMeta parent;
    private final boolean isAbstract;
    private final GenericSignature genericSignature;
    private final TypeMirror extendsType;
    private final String extendsSignature;

    SelectionMeta(TypeElement target, SelectionMeta parent, boolean hasChilds, GenericSignature genericSignature, Types types) {
        this.target = target;
        this.parent = parent;
        this.genericSignature = genericSignature;
        this.isAbstract = target.getModifiers().stream().anyMatch(Modifier.ABSTRACT::equals);
        this.extendsType = getExtendsType(types);
        if (
            (isAbstract || hasChilds) && (
                parent == null ||
                (parent.genericSignature.getSelfVariable() != null && parent.genericSignature.sizeWithoutSelfVariable() == 0 || !extendsTypeEmpty())
            )
        ) {
            genericSignature.createSelfVariable();
        }
        this.extendsSignature = resolveExtendsSignature();
    }

    private boolean extendsTypeEmpty() {
        return extendsType.getKind() == TypeKind.NONE || ((DeclaredType) extendsType).getTypeArguments().isEmpty();
    }

    private String resolveExtendsSignature() {
        if (parent == null) {
            if (genericSignature.getSelfVariable() != null)
                return genericSignature.getSelfVariable(); // first class in the hierarchy of selectors/mappers
            return target.getQualifiedName().toString(); // no child and not abstract class
        } else {
            if (parent.genericSignature.sizeWithoutSelfVariable() != 0) { // parent's generic signature contains self variable and at least one type variable
                if (extendsTypeEmpty()) // raw use
                    return "";
                else {
                    if (this.genericSignature.sizeWithoutSelfVariable() == 0) { // no type variables declared on this class
                        String var = this.genericSignature.getSelfVariable();
                        String res = extendsType.toString();
                        int i = res.indexOf('<');
                        String s = res.substring(i + 1, res.length() - 1);
                        if (var == null) { // no child and not abstract class
                            return target.getQualifiedName().toString() + ", " + s;
                        } else {
                            return var + ", " + s;
                        }
                    } else {

                    }
                }
            } else { // parent's generic signature contains either no type variables or only self variable
                if (parent.genericSignature.isEmpty()) // no variables...
                    return "";
                else { // parent's generic signature contains only self variable
                    if (this.genericSignature.isEmpty()) { // this class is not abstract, does not have child and no type variables declared on it
                        return target.getQualifiedName().toString();
                    } else {
                        String var = this.genericSignature.getSelfVariable();
                        if (var == null) { // no child and not abstract
                            return target.getQualifiedName().toString() + genericSignature.varsToString();
                        } else { // has child or abstract
                            return var;
                        }
                    }
                }
            }
        }
        return null;
    }

    private TypeMirror getExtendsType(Types types) {
        if (parent == null)
            return types.getNoType(TypeKind.NONE);
        TypeMirror parentType = null;
        TypeMirror parentErasure = types.erasure(parent.target.asType());
        for (TypeMirror mirror : types.directSupertypes(target.asType())) {
            if (types.isSameType(types.erasure(mirror), parentErasure)) {
                parentType = mirror;
                break;
            }
        }
        return parentType;
    }

    Name getTargetPackage() {
        return ((PackageElement) target.getEnclosingElement()).getQualifiedName();
    }

    TypeElement getTarget() {
        return target;
    }

}
