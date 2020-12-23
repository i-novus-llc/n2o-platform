package net.n2oapp.platform.selection.core;

import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.List;

class SelectionDescriptor {

    final ResolvableType type;
    final List<SelectionAccessor> accessors;

    SelectionDescriptor(ResolvableType type, List<SelectionAccessor> accessors) {
        this.type = type;
        this.accessors = accessors;
    }

    static class SelectionAccessor {

        final String selectionKey;
        final Method selectionEnumAccessor;
        final Method nestedSelectionAccessor;

        SelectionAccessor(String selectionKey, Method selectionEnumAccessor, Method nestedSelectionAccessor) {
            this.selectionKey = selectionKey;
            this.selectionEnumAccessor = selectionEnumAccessor;
            this.nestedSelectionAccessor = nestedSelectionAccessor;
        }

    }

}
