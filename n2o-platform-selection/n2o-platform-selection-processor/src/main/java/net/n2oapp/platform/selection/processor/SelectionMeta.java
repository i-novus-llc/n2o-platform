package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
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
    private final String mapperTargetSignature;

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
        this.mapperTargetSignature = resolveMapperTargetSignature();
    }

    private String resolveMapperTargetSignature() {
        if (genericSignature.getSelfVariable() != null)
            return genericSignature.getSelfVariable();
        return target.getQualifiedName().toString() + genericSignature.varsToString();
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
                        String temp = getGenerics(extendsType);
                        if (var == null) { // no children and not abstract class
                            return target.getQualifiedName().toString() + ", " + temp;
                        } else {
                            return var + ", " + temp;
                        }
                    } else {
                        String var = this.genericSignature.getSelfVariable();
                        String temp = var == null ? target.getQualifiedName().toString() + genericSignature.varsToString() : var;
                        return temp + ", " + getGenerics(extendsType);
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

    private String getGenerics(TypeMirror type) {
        String res = type.toString();
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

    PackageElement getTargetPackage() {
        Element temp = target;
        while (!(temp.getEnclosingElement() instanceof PackageElement)) {
            temp = temp.getEnclosingElement();
        }
        return (PackageElement) temp.getEnclosingElement();
    }

    TypeElement getTarget() {
        return target;
    }

    public void addProperty(String key, TypeMirror type, SelectionMeta nested) {
        if (nested == null)
            properties.add(new SelectionProperty(key));
        else {
            String fieldTypeSignature = null;
            if (type instanceof WildcardType) {
                type = ((WildcardType) type).getExtendsBound();
            }
            if (type instanceof TypeVariable) {

            } else {
                if (nested.genericSignature.sizeWithoutSelfVariable() == 0) {
                    if (nested.genericSignature.getSelfVariable() != null)
                        fieldTypeSignature = "? extends " + nested.target.getQualifiedName().toString();
                    else
                        fieldTypeSignature = "";
                } else {
                    DeclaredType declaredType = (DeclaredType) type;
                    if (declaredType.getTypeArguments().isEmpty())
                        fieldTypeSignature = ""; // raw use
                    else {
                        if (nested.genericSignature.getSelfVariable() != null) {
                            fieldTypeSignature = "? extends " + type.toString() + ", " + getGenerics(type);
                        } else {
                            fieldTypeSignature = getGenerics(type);
                        }
                    }
                }
            }
            properties.add(new SelectionProperty(key, fieldTypeSignature, nested));
        }
    }

    GenericSignature getGenericSignature() {
        return genericSignature;
    }

}
