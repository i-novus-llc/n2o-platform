package net.n2oapp.platform.selection.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.n2oapp.platform.selection.processor.ProcessorUtil.toposort;

@SupportedAnnotationTypes("net.n2oapp.platform.selection.api.NeedSelection")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SelectionProcessor extends AbstractProcessor {

    private GenericSignatureExtractor genericSignatureExtractor;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.genericSignatureExtractor = new GenericSignatureExtractor(processingEnv.getTypeUtils());
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
        Types types = processingEnv.getTypeUtils();
        for (Element element : elements) {
            if (!valid(element))
                return false;
        }
        elements = toposort(elements);
        for (Element element : elements) {
            if (process(metalist, types, element))
                return false;
        }
        for (SelectionMeta meta : metalist) {
            serialize(meta);
        }
        return false;
    }

    private void serialize(SelectionMeta meta) {
//      TODO
    }

    private boolean process(List<SelectionMeta> metalist, Types types, Element element) {
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superclass = typeElement.getSuperclass();
        SelectionMeta parent = null;
        if (!(superclass instanceof NoType)) {
            Element declaredType = ((DeclaredType) superclass).asElement();
            TypeMirror erasure = types.erasure(superclass);
            if (
                declaredType.getKind() == ElementKind.CLASS &&
                !((TypeElement) declaredType).getQualifiedName().toString().equals("java.lang.Object")
            ) {
                Optional<SelectionMeta> opt = metalist.stream().filter(meta -> types.isSameType(erasure, types.erasure(meta.getTarget().asType()))).findAny();
                if (opt.isEmpty()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Superclass is missing @NeedSelection annotation", element);
                    return true;
                }
                parent = opt.get();
            }
        }
        GenericSignature genericSignature = genericSignatureExtractor.visit(typeElement.asType(), new GenericSignature(typeElement));
        SelectionMeta selectionMeta = new SelectionMeta(typeElement, parent, genericSignature, types);
        metalist.add(selectionMeta);
        return false;
    }

    private boolean valid(Element element) {
        boolean result = true;
        ElementKind kind = element.getKind();
        if (kind != ElementKind.CLASS) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@NeedSelection must be placed only on class", element);
            result = false;
        } else {
            TypeElement typeElement = (TypeElement) element;
            NestingKind nesting = typeElement.getNestingKind();
            if (nesting != NestingKind.TOP_LEVEL && nesting != NestingKind.MEMBER) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@NeedSelection must be placed either on TOP_LEVEL class or INNER class", element);
                result = false;
            }
        }
        return result;
    }


}
