package net.n2oapp.platform.selection.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

import static net.n2oapp.platform.selection.processor.ProcessorUtil.toposort;

@SupportedAnnotationTypes("net.n2oapp.platform.selection.api.NeedSelection")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SelectionProcessor extends AbstractProcessor {

    private Types types;
    private TypeMirror collection;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        this.collection = types.erasure(processingEnv.getElementUtils().getTypeElement("java.util.Collection").asType());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty())
            return false;
        TypeElement needSelection = annotations.iterator().next();
        List<? extends Element> elements = new ArrayList<>(roundEnv.getElementsAnnotatedWith(needSelection));
        if (elements.isEmpty())
            return false;
        List<SelectionMeta> metalist = new ArrayList<>(elements.size());
        for (Element element : elements) {
            if (!valid(element))
                return false;
        }
        List<Map.Entry<? extends Element, Boolean>> toposort = toposort(elements);
        for (Map.Entry<? extends Element, Boolean> entry : toposort) {
            if (init(metalist, entry))
                return false;
        }
        for (SelectionMeta entry : metalist) {
            processFields(metalist, entry);
        }
        for (SelectionMeta meta : metalist) {
            serialize(meta);
        }
        return false;
    }

    private void processFields(List<SelectionMeta> metalist, SelectionMeta entry) {
        Element target = entry.getTarget();
        for (Element member : target.getEnclosedElements()) {
            if (member.getKind() == ElementKind.FIELD) {
                TypeMirror fieldType = member.asType();
                if (!types.isAssignable(fieldType, collection)) {
                    SelectionMeta assignableType = findMostSpecificAssignableType(metalist, fieldType);
                }
            }
        }
    }

    private SelectionMeta findMostSpecificAssignableType(List<SelectionMeta> metalist, TypeMirror type) {
        String str = type.toString();
        if (str.startsWith("java.") || str.startsWith("javax."))
            return null;
        SelectionMeta result = null;
        for (SelectionMeta meta : metalist) {
            TypeMirror mirror = meta.getTarget().asType();
            if (!types.isAssignable(type, mirror))
                continue;
            if (result == null || !types.isAssignable(mirror, result.getTarget().asType())) {
                result = meta;
            }
        }
        return result;
    }

    private void serialize(SelectionMeta meta) {
//      TODO
    }

    private boolean init(List<SelectionMeta> metalist, Map.Entry<? extends Element, Boolean> entry) {
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
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Superclass is missing @NeedSelection annotation", entry.getKey());
                return true;
            }
            parent = opt.get();
        }
        GenericSignature signature = getGenericSignature(element, (DeclaredType) element.asType());
        SelectionMeta result = new SelectionMeta(element, parent, entry.getValue(), signature, types);
        metalist.add(result);
        return false;
    }

    private GenericSignature getGenericSignature(TypeElement typeElement, DeclaredType type) {
        GenericSignature signature = new GenericSignature(typeElement);
        if (!type.getTypeArguments().isEmpty()) {
            for (TypeMirror arg : type.getTypeArguments()) {
                TypeVariable var = (TypeVariable) arg;
                TypeMirror bound = var.getUpperBound();
                signature.addTypeVariable(var.toString(), bound.toString());
            }
        }
        return signature;
    }

    private boolean valid(Element element) {
        boolean result = true;
        if (element.getKind() != ElementKind.CLASS) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@NeedSelection must be placed only on DTO class", element);
            result = false;
        } else {
            TypeElement typeElement = (TypeElement) element;
            NestingKind nesting = typeElement.getNestingKind();
            if (nesting != NestingKind.TOP_LEVEL && nesting != NestingKind.MEMBER) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@NeedSelection must be placed either on top level class or inner DTO class", element);
                result = false;
            }
        }
        return result;
    }


}
