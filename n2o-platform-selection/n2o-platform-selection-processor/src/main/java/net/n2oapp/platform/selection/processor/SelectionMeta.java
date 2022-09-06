package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.Selection;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import java.util.*;

class SelectionMeta {

    private final TypeElement target;
    private final SelectionMeta parent;
    private final List<SelectionMeta> children = new ArrayList<>();
    private final GenericSignature selectionGenericSignature;
    private final GenericSignature fetcherGenericSignature;
    private final GenericSignature joinerGenericSignature;
    private final GenericSignature spyGenericSignature;
    private final TypeMirror extendsType;
    private final String selectionExtendsSignature;
    private final String joinerExtendsSignature;
    private final String fetcherExtendsSignature;
    private final String spyExtendsSignature;
    private final String idTypeVariable;
    private final String entityTypeVariable;
    private final String modelType;
    private final String fetcherTypeVariable;
    private final String selectionTypeVariable;
    private final String selectionType;
    private final String fetcherType;
    private final LinkedHashMap<String, SelectionProperty> properties;
    private final LinkedHashMap<String, SelectionProperty> unresolvedProperties;
    private final boolean isAbstract;
    private final String prefix;

    private String jacksonTypeTag;

    SelectionMeta(
            TypeElement target,
            SelectionMeta parent,
            boolean hasChildren,
            GenericSignature selectionGenericSignature,
            Types types,
            String prefix
    ) {
        this.target = target;
        this.parent = parent;
        this.selectionGenericSignature = selectionGenericSignature;
        if (prefix == null) {
            String str = target.getSimpleName().toString();
            prefix = Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
        this.prefix = prefix.trim();
        this.properties = new LinkedHashMap<>();
        this.unresolvedProperties = new LinkedHashMap<>();
        this.isAbstract = target.getModifiers().stream().anyMatch(Modifier.ABSTRACT::equals);
        this.extendsType = getExtendsType(types);
        if (isAbstract || hasChildren) {
            selectionGenericSignature.createSelfVariable();
        }
        this.selectionExtendsSignature = resolveExtendsSignature();
        this.modelType = resolveModelType();
        this.spyGenericSignature = selectionGenericSignature.copy();
        if (selectionGenericSignature.getSelfVariable() != null) {
            this.selectionTypeVariable = spyGenericSignature.allocateVar("S");
            this.spyGenericSignature.addTypeVariable(this.selectionTypeVariable, resolveSelectionType());
            this.selectionType = selectionTypeVariable;
        } else {
            selectionTypeVariable = null;
            this.selectionType = resolveSelectionType();
        }
        this.spyExtendsSignature = join(selectionExtendsSignature, selectionTypeVariable == null ? selectionType : selectionTypeVariable);
        this.fetcherGenericSignature = spyGenericSignature.copy();
        this.entityTypeVariable = fetcherGenericSignature.allocateVar("E");
        this.fetcherGenericSignature.addTypeVariable(entityTypeVariable, null);
        this.fetcherExtendsSignature = join(spyExtendsSignature, entityTypeVariable);
        this.joinerGenericSignature = fetcherGenericSignature.copy();
        if (selectionGenericSignature.getSelfVariable() != null) {
            this.fetcherTypeVariable = joinerGenericSignature.allocateVar("F");
            this.fetcherType = fetcherTypeVariable;
            this.joinerGenericSignature.addTypeVariable(fetcherTypeVariable, resolveFetcherType());
        } else {
            this.fetcherType = resolveFetcherType();
            this.fetcherTypeVariable = null;
        }
        this.idTypeVariable = joinerGenericSignature.allocateVar("ID");
        this.joinerGenericSignature.addTypeVariable(idTypeVariable, null);
        this.joinerExtendsSignature = join(fetcherExtendsSignature, fetcherTypeVariable == null ? fetcherType : fetcherTypeVariable, idTypeVariable);
    }

    private String join(String...strs) {
        return String.join(", ", strs);
    }

    String getPrefix() {
        return prefix;
    }

    String getPrefixOrGenerate() {
        if (prefix == null || "".equals(prefix.trim())) {
            String str = target.getSimpleName().toString();
            return Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
        return prefix;
    }

    private String resolveModelType() {
        if (selectionGenericSignature.getSelfVariable() != null)
            return selectionGenericSignature.getSelfVariable();
        return target.getQualifiedName().toString() + selectionGenericSignature.varsToString(false);
    }

    private String resolveSelectionType() {
        PackageElement targetPackage = getTargetPackage();
        return targetPackage + "." + getTarget().getSimpleName().toString() + Selection.class.getSimpleName() + selectionGenericSignature.varsToString(true);
    }

    private String resolveFetcherType() {
        PackageElement targetPackage = getTargetPackage();
        return targetPackage + "." + getTarget().getSimpleName().toString() + Fetcher.class.getSimpleName() + fetcherGenericSignature.varsToString(true);
    }

    private boolean extendsTypeEmpty() {
        return extendsType.getKind() == TypeKind.NONE || ((DeclaredType) extendsType).getTypeArguments().isEmpty();
    }

    private String resolveExtendsSignature() {
        if (parent == null) {
            if (selectionGenericSignature.getSelfVariable() != null)
                return selectionGenericSignature.getSelfVariable(); // first class in the hierarchy
            return target.getQualifiedName().toString(); // no children and not abstract class
        } else {
            if (!parent.selectionGenericSignature.noGenericsDeclared()) { // parent's generic signature contains self variable and at least one type variable
                if (extendsTypeEmpty()) { // raw use
                    throw new RawUseException();
                } else {
                    if (this.selectionGenericSignature.noGenericsDeclared()) { // no type variables declared on this class
                        String var = this.selectionGenericSignature.getSelfVariable();
                        String temp = getGenerics(extendsType.toString());
                        if (var == null) { // no children and not abstract class
                            return target.getQualifiedName().toString() + ", " + temp;
                        } else {
                            return var + ", " + temp;
                        }
                    } else {
                        String var = this.selectionGenericSignature.getSelfVariable();
                        String temp = var == null ? target.getQualifiedName().toString() + selectionGenericSignature.varsToString(true) : var;
                        return temp + ", " + getGenerics(extendsType.toString());
                    }
                }
            } else { // parent's generic signature contains only self variable
                if (this.selectionGenericSignature.isEmpty()) { // this class is not abstract, doesn't have children and no type variables declared on it
                    return target.getQualifiedName().toString();
                } else {
                    String var = this.selectionGenericSignature.getSelfVariable();
                    if (var == null) { // no children and not abstract
                        return target.getQualifiedName().toString() + selectionGenericSignature.varsToString(true);
                    } else { // has children or abstract
                        return var;
                    }
                }
            }
        }
    }

    private String getGenerics(String type) {
        if (type == null || "".equals(type.trim()))
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

    List<SelectionMeta> getChildren() {
        return children;
    }

    void addChildren(List<SelectionMeta> children) {
        this.children.addAll(children);
        this.children.sort(Comparator.comparing(meta -> meta.getTarget().getSimpleName().toString()));
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

    void addProperty(
        String name,
        Element member,
        TypeMirror originalType,
        TypeMirror modelType,
        SelectionMeta selection,
        TypeMirror collectionType,
        boolean joined,
        boolean withNestedJoiner,
        boolean joinOnly
    ) {
        if (selection == null)
            properties.put(name, new SelectionProperty(name, originalType, member, joined, joinOnly));
        else {
            String generics;
            if (modelType instanceof WildcardType) {
                modelType = ((WildcardType) modelType).getExtendsBound();
            }
            LinkedHashMap<String, SelectionProperty> targetProperties;
            if (TypeUtil.containsTypeVariables(modelType)) {
                targetProperties = unresolvedProperties;
                generics = null;
            } else {
                targetProperties = properties;
                if (selection.selectionGenericSignature.noGenericsDeclared()) {
                    if (selection.selectionGenericSignature.getSelfVariable() != null)
                        generics = selection.target.getQualifiedName().toString();
                    else
                        generics = "";
                } else {
                    DeclaredType declaredType = (DeclaredType) modelType;
                    if (declaredType.getTypeArguments().isEmpty()) {
                        throw new RawUseException();
                    } else {
                        if (selection.selectionGenericSignature.getSelfVariable() != null) {
                            generics = modelType.toString() + ", " + getGenerics(modelType.toString());
                        } else {
                            generics = getGenerics(modelType.toString());
                        }
                    }
                }
            }
            targetProperties.put(name, new SelectionProperty(name, member, originalType, modelType, selection, generics, collectionType, joined, withNestedJoiner, joinOnly));
        }
    }

    Collection<SelectionProperty> getProperties() {
        return properties.values();
    }

    public Collection<SelectionProperty> getUnresolvedProperties() {
        return unresolvedProperties.values();
    }

    SelectionMeta getParent() {
        return parent;
    }

    String getExtendsSignatureNoBrackets() {
        return selectionExtendsSignature;
    }

    String getSelectionExtendsSignature() {
        return getExtendsSignature(selectionExtendsSignature);
    }

    String getFetcherExtendsSignature() {
        return getExtendsSignature(fetcherExtendsSignature);
    }

    String getJoinerExtendsSignature() {
        return getExtendsSignature(joinerExtendsSignature);
    }

    String getSpyExtendsSignature() {
        return getExtendsSignature(spyExtendsSignature);
    }

    private String getExtendsSignature(String signature) {
        if (signature.isEmpty())
            return "";
        return "<" + signature + ">";
    }

    String getModelType() {
        return modelType;
    }

    String getJacksonTypeTag() {
        return jacksonTypeTag;
    }

    void addJacksonTyping() {
        if (jacksonTypeTag == null) {
            if (parent != null)
                parent.addJacksonTyping();
            else {
                spreadJacksonTyping(0);
            }
        }
    }

    private int spreadJacksonTyping(int typeId) {
        if (!isAbstract) {
            this.jacksonTypeTag = Integer.toString(typeId++);
        }
        for (SelectionMeta meta : children) {
            typeId = meta.spreadJacksonTyping(typeId);
        }
        return typeId;
    }

    boolean isAbstract() {
        return isAbstract;
    }

    GenericSignature getSelectionGenericSignature() {
        return selectionGenericSignature;
    }

    GenericSignature getFetcherGenericSignature() {
        return fetcherGenericSignature;
    }

    GenericSignature getJoinerGenericSignature() {
        return joinerGenericSignature;
    }

    GenericSignature getSpyGenericSignature() {
        return spyGenericSignature;
    }

    String getSelectionTypeVariable() {
        return selectionTypeVariable;
    }

    String getFetcherTypeVariable() {
        return fetcherTypeVariable;
    }

    String getIdTypeVariable() {
        return idTypeVariable;
    }

    String getEntityTypeVariable() {
        return entityTypeVariable;
    }

    String getFetcherType() {
        return fetcherType;
    }

    String getSelectionType() {
        return selectionType;
    }

    boolean containsResolvedProperty(String name) {
        return properties.containsKey(name);
    }

}
