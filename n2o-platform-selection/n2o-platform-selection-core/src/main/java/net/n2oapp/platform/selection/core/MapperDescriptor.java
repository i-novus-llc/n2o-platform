package net.n2oapp.platform.selection.core;

import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.Map;

class MapperDescriptor {

    final ResolvableType type;
    final Map<String, MapperAccessor> accessors;

    MapperDescriptor(ResolvableType type, Map<String, MapperAccessor> accessors) {
        this.type = type;
        this.accessors = accessors;
    }

    static class MapperAccessor {

        final String selectionKey;
        final Method selectMethod;
        final Method nestedMapperAccessor;
        final boolean isCollectionMapper;

        MapperAccessor(String selectionKey, Method selectMethod, Method nestedMapperAccessor, boolean isCollectionMapper) {
            this.selectionKey = selectionKey;
            this.selectMethod = selectMethod;
            this.nestedMapperAccessor = nestedMapperAccessor;
            this.isCollectionMapper = isCollectionMapper;
        }

    }

}
