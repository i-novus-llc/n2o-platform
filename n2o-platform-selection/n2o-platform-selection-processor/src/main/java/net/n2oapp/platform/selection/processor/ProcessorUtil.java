package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Selective;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.*;

import static java.util.Collections.emptyList;

final class ProcessorUtil {

    private ProcessorUtil() {
    }

    static LinkedList<Map.Entry<Element, List<Element>>> toposort(List<? extends Element> elements) {
        Map<Element, List<Element>> graph = new HashMap<>();
        for (Element elem : elements) {
            graph.putIfAbsent(elem, null);
            TypeElement asTypeElem = (TypeElement) elem;
            DeclaredType superclass = (DeclaredType) asTypeElem.getSuperclass();
            Element superelem = superclass.asElement();
            if (superelem.getAnnotation(Selective.class) != null) {
                graph.compute(superelem, (ignored, list) -> {
                    if (list == null)
                        return new ArrayList<>(Collections.singletonList(elem));
                    list.add(elem);
                    return list;
                });
            }
        }
        return toposort(graph);
    }

    private static LinkedList<Map.Entry<Element, List<Element>>> toposort(Map<Element, List<Element>> graph) {
        Set<Element> visited = new HashSet<>();
        LinkedList<Map.Entry<Element, List<Element>>> stack = new LinkedList<>();
        for (Map.Entry<Element, List<Element>> e : graph.entrySet()) {
            if (!visited.contains(e.getKey())) {
                toposort(stack, visited, graph, e.getKey());
            }
        }
        return stack;
    }

    private static void toposort(LinkedList<Map.Entry<Element, List<Element>>> stack, Set<Element> visited, Map<Element, List<Element>> graph, Element elem) {
        visited.add(elem);
        List<Element> children = graph.get(elem);
        if (children != null) {
            for (Element e : graph.get(elem)) {
                if (!visited.contains(e))
                    toposort(stack, visited, graph, e);
            }
        }
        stack.push(Map.entry(elem, children == null ? emptyList() : children));
    }

}
