package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.SelectionIgnore;
import net.n2oapp.platform.selection.api.Selective;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static net.n2oapp.platform.selection.processor.ProcessorUtil.toposort;

@SupportedAnnotationTypes("net.n2oapp.platform.selection.api.Selective")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SelectionProcessor extends AbstractProcessor {

    private static final String ADD_JACKSON_TYPING = "net.n2oapp.platform.selection.addJacksonTyping";
    private static final String ADD_JAXRS_ANNOTATIONS = "net.n2oapp.platform.selection.addJaxRsAnnotations";
    private static final String OVERRIDE_SELECTION_KEYS = "net.n2oapp.platform.selection.overrideSelectionKeys";

    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
        ADD_JACKSON_TYPING,
        ADD_JAXRS_ANNOTATIONS,
        OVERRIDE_SELECTION_KEYS
    );

    private Types types;
    private TypeMirror collection;

    private boolean addJacksonTyping;

    private SelectionSerializer selectionSerializer;
    private FetcherSerializer fetcherSerializer;
    private JoinerSerializer joinerSerializer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();
        this.collection = types.erasure(elements.getTypeElement("java.util.Collection").asType());
        TypeElement jsonTypeInfo = elements.getTypeElement("com.fasterxml.jackson.annotation.JsonTypeInfo");
        TypeElement jsonSubTypes = elements.getTypeElement("com.fasterxml.jackson.annotation.JsonSubTypes");
        TypeElement requestParam = elements.getTypeElement("javax.ws.rs.QueryParam");
        TypeElement beanParam = elements.getTypeElement("javax.ws.rs.BeanParam");
        this.addJacksonTyping = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(ADD_JACKSON_TYPING, Boolean.toString(jsonTypeInfo != null)));
        boolean addJaxRsAnnotations = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(ADD_JAXRS_ANNOTATIONS, Boolean.toString(requestParam != null)));
        boolean overrideSelectionKeys = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(OVERRIDE_SELECTION_KEYS, Boolean.toString(true)));
        this.selectionSerializer = new SelectionSerializer(addJacksonTyping, addJaxRsAnnotations, overrideSelectionKeys, jsonTypeInfo, jsonSubTypes, requestParam, beanParam);
        this.fetcherSerializer = new FetcherSerializer();
        this.joinerSerializer = new JoinerSerializer();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty())
            return false;
        TypeElement selective = annotations.iterator().next();
        List<? extends Element> elements = new ArrayList<>(roundEnv.getElementsAnnotatedWith(selective));
        if (elements.isEmpty())
            return false;
        for (Element element : elements) {
            if (!valid(element))
                return false;
        }
        List<SelectionMeta> metalist = new ArrayList<>(elements.size());
        LinkedList<Map.Entry<Element, List<Element>>> toposort = toposort(elements);
        Map<Element, SelectionMeta> index = new HashMap<>();
        for (Map.Entry<Element, List<Element>> entry : toposort) {
            if (init(metalist, entry))
                return false;
            index.put(entry.getKey(), metalist.get(metalist.size() - 1));
        }
        for (SelectionMeta meta : metalist) {
            List<Element> children = toposort.stream().filter(elem -> elem.getKey().equals(meta.getTarget())).findFirst().orElseThrow().getValue();
            meta.addChildren(metalist.stream().filter(other -> children.contains(other.getTarget())).collect(toList()));
        }
        for (SelectionMeta meta : metalist) {
            processProperties(metalist, meta, index);
        }
        if (addJacksonTyping) {
            for (SelectionMeta meta : metalist) {
                if (!meta.getChildren().isEmpty()) {
                    meta.addJacksonTyping();
                }
            }
        }
        for (SelectionMeta meta : metalist) {
            serialize(meta);
        }
        return false;
    }

    private void processProperties(List<SelectionMeta> metalist, SelectionMeta meta, Map<Element, SelectionMeta> index) {
        Element target = meta.getTarget();
        for (Element member : target.getEnclosedElements()) {
            if (member.getKind() == ElementKind.FIELD && member.getModifiers().stream().noneMatch(Modifier.STATIC::equals)) {
                if (member.getAnnotation(SelectionIgnore.class) != null)
                    continue;
                processProperty(metalist, meta, member);
            }
        }
        SelectionMeta curr = meta.getParent();
        DeclaredType declaredType = ((DeclaredType) meta.getTarget().asType());
        while (curr != null) {
            resolveUnresolvedProperties(meta, index, curr, declaredType);
            curr = curr.getParent();
        }
    }

    private void resolveUnresolvedProperties(SelectionMeta meta, Map<Element, SelectionMeta> index, SelectionMeta curr, DeclaredType declaredType) {
        for (SelectionProperty property : curr.getUnresolvedProperties()) {
            TypeMirror mirror = processingEnv.getTypeUtils().asMemberOf(declaredType, property.getMember());
            if (property.getCollectionType() != null) {
                mirror = ((DeclaredType) mirror).getTypeArguments().get(0);
                if (mirror instanceof WildcardType)
                    mirror = ((WildcardType) mirror).getExtendsBound();
            }
            SelectionMeta nested = index.get(types.asElement(mirror));
            if (nested == null || TypeUtil.containsTypeVariables(mirror) || containsResolvedProperty(meta, property.getName(), curr))
                continue;
            try {
                meta.addProperty(
                    property.getName(),
                    property.getMember(),
                    property.getOriginalType(),
                    mirror,
                    nested,
                    property.getCollectionType(),
                    property.isJoined(),
                    property.isWithNestedJoiner()
                );
            } catch (RawUseException ignored) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Raw use unsupported", meta.getTarget());
            }
        }
    }

    private void processProperty(List<SelectionMeta> metalist, SelectionMeta meta, Element member) {
        TypeMirror collectionType = null;
        final TypeMirror originalType = member.asType();
        TypeMirror type = originalType;
        TypeMirror erased = types.erasure(type);
        SelectionMeta nested = null;
        if (!types.isAssignable(erased, collection)) {
            nested = findNestedSelection(metalist, erased);
        } else {
            if (!erased.toString().equals("java.util.List") && !erased.toString().equals("java.util.Set")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unsupported collection. java.util.List or java.util.Set must be used", member);
            }
            DeclaredType declaredType = (DeclaredType) member.asType();
            List<? extends TypeMirror> args = declaredType.getTypeArguments();
            if (!args.isEmpty()) {
                TypeMirror arg = args.get(0);
                nested = findNestedSelection(metalist, types.erasure(arg));
                type = arg;
                collectionType = erased;
            }
        }
        Joined joinedAnno = member.getAnnotation(Joined.class);
        boolean isJoined = joinedAnno != null;
        boolean withNestedJoiner = isJoined && joinedAnno.withNestedJoiner();
        if (withNestedJoiner && nested == null)
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Nested joiner can be placed only on nested SELECTIVE properties", member);
        String name = member.getSimpleName().toString();
        try {
            meta.addProperty(
                name,
                member,
                originalType,
                type,
                nested,
                collectionType,
                isJoined,
                withNestedJoiner
            );
        } catch (RawUseException ignored) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Raw use unsupported", member);
        }
    }

    private boolean containsResolvedProperty(SelectionMeta target, String propertyName, SelectionMeta searchUpTo) {
        SelectionMeta curr = target;
        do {
            if (curr.containsResolvedProperty(propertyName))
                return true;
            curr = curr.getParent();
        } while (curr != searchUpTo);
        return false;
    }

    private SelectionMeta findNestedSelection(List<SelectionMeta> metalist, TypeMirror type) {
        String str = type.toString();
        if (str.startsWith("java.") || str.startsWith("javax."))
            return null;
        for (SelectionMeta meta : metalist) {
            TypeMirror mirror = meta.getTarget().asType();
            if (types.isSameType(types.erasure(mirror), type))
                return meta;
        }
        return null;
    }

    private void serialize(SelectionMeta meta) {
        Filer filer = processingEnv.getFiler();
        try {
            selectionSerializer.serialize(meta, filer);
            fetcherSerializer.serialize(meta, filer);
            joinerSerializer.serialize(meta, filer);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private boolean init(List<SelectionMeta> metalist, Map.Entry<Element, List<Element>> entry) {
        TypeElement element = (TypeElement) entry.getKey();
        TypeMirror superclass = element.getSuperclass();
        SelectionMeta parent = null;
        Element superType = ((DeclaredType) superclass).asElement();
        TypeMirror superErasure = types.erasure(superclass);
        if (
            superType.getKind() == ElementKind.CLASS &&
            !((TypeElement) superType).getQualifiedName().toString().equals("java.lang.Object")
        ) {
            Optional<SelectionMeta> opt = metalist.stream().filter(meta -> types.isSameType(superErasure, types.erasure(meta.getTarget().asType()))).findAny();
            if (opt.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Superclass is missing " + Selective.class + " annotation", entry.getKey());
                return true;
            }
            parent = opt.get();
        }
        GenericSignature signature;
        try {
            signature = getGenericSignature(element, (DeclaredType) element.asType());
        } catch (RawUseException ignored) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Raw type usage not supported", element);
            return true;
        }
        String prefix = element.getAnnotation(Selective.class).prefix();
        if (prefix.equals(Selective.NULL))
            prefix = null;
        SelectionMeta result;
        try {
            result = new SelectionMeta(element, parent, !entry.getValue().isEmpty(), signature, types, prefix);
        } catch (RawUseException ignored) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Raw type inheritance not permitted", element);
            return true;
        }
        metalist.add(result);
        return false;
    }

    private GenericSignature getGenericSignature(TypeElement typeElement, DeclaredType type) {
        GenericSignature signature = new GenericSignature(typeElement);
        if (!type.getTypeArguments().isEmpty()) {
            for (TypeMirror arg : type.getTypeArguments()) {
                TypeVariable var = (TypeVariable) arg;
                if (raw(var, new HashSet<>()))
                    throw new RawUseException();
                TypeMirror bound = var.getUpperBound();
                signature.addTypeVariable(var.toString(), bound.toString());
            }
        }
        return signature;
    }

    private boolean raw(TypeVariable var, Set<TypeMirror> seen) {
        if (seen.contains(var))
            return false;
        seen.add(var);
        TypeMirror t = var.getUpperBound();
        if (t instanceof TypeVariable)
            return raw((TypeVariable) t, seen);
        DeclaredType provided = (DeclaredType) t;
        DeclaredType declared = (DeclaredType) types.asElement(t).asType();
        if (!declared.getTypeArguments().isEmpty() && provided.getTypeArguments().isEmpty())
            return true;
        for (TypeMirror mirror : provided.getTypeArguments()) {
            if (mirror instanceof TypeVariable && raw((TypeVariable) mirror, seen))
                return true;
        }
        return false;
    }

    private boolean valid(Element element) {
        boolean result = true;
        if (element.getKind() != ElementKind.CLASS) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Selective.class + " must be placed only on DTO class", element);
            result = false;
        } else {
            TypeElement typeElement = (TypeElement) element;
            NestingKind nesting = typeElement.getNestingKind();
            if (nesting != NestingKind.TOP_LEVEL && nesting != NestingKind.MEMBER) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Selective.class + " must be placed either on top level class or inner DTO class", element);
                result = false;
            }
        }
        return result;
    }

}
