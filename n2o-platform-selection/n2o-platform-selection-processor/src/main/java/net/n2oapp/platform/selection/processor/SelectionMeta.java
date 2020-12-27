package net.n2oapp.platform.selection.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SelectionMeta {

    public static final String EXTENDS = "? extends ";

    private final TypeElement target;
    private final SelectionMeta parent;
    private final GenericSignature genericSignature;
    private final TypeMirror extendsType;
    private final String extendsSignature;
    private final List<SelectionProperty> properties;
    private final String mapperTarget;

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
                (parent.genericSignature.getSelfVariable() != null && (parent.genericSignature.noGenericsDeclared() || !extendsTypeEmpty()))
            )
        ) {
            genericSignature.createSelfVariable();
        }
        this.extendsSignature = resolveExtendsSignature();
        this.mapperTarget = resolveMapperTarget();
    }

    private String resolveMapperTarget() {
        if (genericSignature.getSelfVariable() != null)
            return genericSignature.getSelfVariable();
        return target.getQualifiedName().toString() + genericSignature.varsToString(false);
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
            if (!parent.genericSignature.noGenericsDeclared()) { // parent's generic signature contains self variable and at least one type variable
                if (extendsTypeEmpty()) // raw use
                    return "";
                else {
                    if (this.genericSignature.noGenericsDeclared()) { // no type variables declared on this class
                        String var = this.genericSignature.getSelfVariable();
                        String temp = getGenerics(extendsType.toString());
                        if (var == null) { // no children and not abstract class
                            return target.getQualifiedName().toString() + ", " + temp;
                        } else {
                            return var + ", " + temp;
                        }
                    } else {
                        String var = this.genericSignature.getSelfVariable();
                        String temp = var == null ? target.getQualifiedName().toString() + genericSignature.varsToString(true) : var;
                        return temp + ", " + getGenerics(extendsType.toString());
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
                            return target.getQualifiedName().toString() + genericSignature.varsToString(true);
                        } else { // has children or abstract
                            return var;
                        }
                    }
                }
            }
        }
    }

    private String getGenerics(String type) {
        if (type.isBlank())
            return "";
        int i = type.indexOf('<');
        if (i == -1)
            return "";
        return type.substring(i + 1, type.length() - 1);
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

    void addProperty(String key, TypeMirror originalType, TypeMirror type, SelectionMeta nested, TypeMirror collectionRawType) {
        if (nested == null)
            properties.add(new SelectionProperty(key));
        else {
            boolean wildcard = false;
            String nestedGenericSignature;
            if (type instanceof WildcardType) {
                wildcard = true;
                type = ((WildcardType) type).getExtendsBound();
            }
            if (type instanceof TypeVariable) {
                String var = type.toString();
                if (nested.genericSignature.noGenericsDeclared()) {
                    if (nested.genericSignature.getSelfVariable() != null)
                        nestedGenericSignature = (wildcard || collectionRawType == null ? EXTENDS : "") + var;
                    else
                        nestedGenericSignature = "";
                } else {
                    String generics = getGenerics(genericSignature.getVariableBounds(var)[0]);
                    if (nested.genericSignature.getSelfVariable() == null) {
                        nestedGenericSignature = generics;
                    } else {
                        if (generics.isEmpty()) // raw use, fallback to wildcards
                            generics = IntStream.range(0, nested.genericSignature.size() - 1).mapToObj(i -> "?").collect(Collectors.joining(", "));
                        nestedGenericSignature = (wildcard || collectionRawType == null ? EXTENDS : "") + var + ", " + generics;
                    }
                }
            } else {
                if (nested.genericSignature.noGenericsDeclared()) {
                    if (nested.genericSignature.getSelfVariable() != null)
                        nestedGenericSignature = (wildcard || collectionRawType == null ? EXTENDS : "") + nested.target.getQualifiedName().toString();
                    else
                        nestedGenericSignature = "";
                } else {
                    DeclaredType declaredType = (DeclaredType) type;
                    if (declaredType.getTypeArguments().isEmpty())
                        nestedGenericSignature = ""; // raw use
                    else {
                        if (nested.genericSignature.getSelfVariable() != null) {
                            nestedGenericSignature = (wildcard || collectionRawType == null ? EXTENDS : "") + type.toString() + ", " + getGenerics(type.toString());
                        } else {
                            nestedGenericSignature = getGenerics(type.toString());
                        }
                    }
                }
            }
            properties.add(new SelectionProperty(key, nestedGenericSignature, nested, originalType, collectionRawType));
        }
    }

    GenericSignature getGenericSignature() {
        return genericSignature;
    }

    Iterable<SelectionProperty> getProperties() {
        return properties;
    }

    SelectionMeta getParent() {
        return parent;
    }

    String getExtendsSignature() {
        if (extendsSignature.isEmpty())
            return "";
        return "<" + extendsSignature + ">";
    }

    String getMapperTarget() {
        return mapperTarget;
    }

}
