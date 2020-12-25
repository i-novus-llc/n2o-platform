package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

class SelectionMeta {

    private final TypeElement target;
    private final SelectionMeta parent;
    private final GenericSignature genericSignature;
    private final TypeMirror extendsType;
    private final String extendsSignature;
    private final List<SelectionProperty> properties;

    SelectionMeta(TypeElement target, SelectionMeta parent, boolean hasChildren, GenericSignature genericSignature, Types types) {
        this.target = target;
        this.parent = parent;
        this.genericSignature = genericSignature;
        this.properties = new ArrayList<>(0);
        boolean isAbstract = target.getModifiers().stream().anyMatch(Modifier.ABSTRACT::equals);
        this.extendsType = getExtendsType(types);
        if (
            (isAbstract || hasChildren) && (
                parent == null ||
                (parent.genericSignature.getSelfVariable() != null && (parent.genericSignature.sizeWithoutSelfVariable() == 0 || !extendsTypeEmpty()))
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
            return target.getQualifiedName().toString(); // no children and not abstract class
        } else {
            if (parent.genericSignature.sizeWithoutSelfVariable() != 0) { // parent's generic signature contains self variable and at least one type variable
                if (extendsTypeEmpty()) // raw use
                    return "";
                else {
                    if (this.genericSignature.sizeWithoutSelfVariable() == 0) { // no type variables declared on this class
                        String var = this.genericSignature.getSelfVariable();
                        String temp = getExtendsSignature();
                        if (var == null) { // no children and not abstract class
                            return target.getQualifiedName().toString() + ", " + temp;
                        } else {
                            return var + ", " + temp;
                        }
                    } else {
                        String var = this.genericSignature.getSelfVariable();
                        String temp = var == null ? target.getQualifiedName().toString() + genericSignature.varsToString() : var;
                        return temp + ", " + getExtendsSignature();
                    }
                }
            } else { // parent's generic signature contains either no type variables or only self variable
                if (parent.genericSignature.isEmpty()) // no variables...
                    return "";
                else { // parent's generic signature contains only self variable
                    if (this.genericSignature.isEmpty()) { // this class is not abstract, doesn't have children and no type variables declared on it
                        return target.getQualifiedName().toString();
                    } else {
                        String var = this.genericSignature.getSelfVariable();
                        if (var == null) { // no children and not abstract
                            return target.getQualifiedName().toString() + genericSignature.varsToString();
                        } else { // has children or abstract
                            return var;
                        }
                    }
                }
            }
        }
    }

    private String getExtendsSignature() {
        String res = extendsType.toString();
        int i = res.indexOf('<');
        return res.substring(i + 1, res.length() - 1);
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

    public void addProperty(Element member, SelectionMeta nested) {
        String key = member.getSimpleName().toString();
        if (nested == null)
            properties.add(new SelectionProperty(key));
        else {

        }
    }

}
