package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Selective;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.*;

final class ProcessorUtil {

    private ProcessorUtil() {
    }

    static List<Map.Entry<? extends Element, Boolean>> toposort(List<? extends Element> elements) {
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
        return topologicalSort(graph);
    }

    private static List<Map.Entry<? extends Element, Boolean>> topologicalSort(Map<Element, List<Element>> graph) {
        Set<Element> visited = new HashSet<>();
        LinkedList<Map.Entry<? extends Element, Boolean>> stack = new LinkedList<>();
        for (Map.Entry<Element, List<Element>> e : graph.entrySet()) {
            if (!visited.contains(e.getKey())) {
                topologicalSort0(stack, visited, graph, e.getKey());
            }
        }
        return stack;
    }

    private static void topologicalSort0(LinkedList<Map.Entry<? extends Element, Boolean>> stack, Set<Element> visited, Map<Element, List<Element>> graph, Element elem) {
        visited.add(elem);
        List<Element> children = graph.get(elem);
        if (children != null) {
            for (Element e : graph.get(elem)) {
                if (!visited.contains(e))
                    topologicalSort0(stack, visited, graph, e);
            }
        }
        stack.push(Map.entry(elem, children != null && !children.isEmpty()));
    }

}
