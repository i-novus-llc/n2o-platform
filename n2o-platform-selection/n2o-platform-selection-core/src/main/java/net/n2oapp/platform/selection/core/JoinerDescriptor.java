package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Joiner;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Описание {@link Joiner}-а, полученное через Reflection.
 */
class JoinerDescriptor {

    final ResolvableType targetType;
    final Map<String, JoinerAccessor> accessors;
    final ResolvableType fetcherType;
    final Class<? extends Joiner> joinerClass;

    public JoinerDescriptor(ResolvableType targetType, Map<String, JoinerAccessor> accessors, ResolvableType fetcherType, Class<? extends Joiner> joinerClass) {
        this.targetType = targetType;
        this.accessors = accessors;
        this.fetcherType = fetcherType;
        this.joinerClass = joinerClass;
    }

    static class JoinerAccessor {

        final Method joinMethod;
        final Method nestedJoinerAccessor;
        final ResolvableType targetType;
        final ResolvableType collectionType;

        JoinerAccessor(Method joinMethod, Method nestedJoinerAccessor, ResolvableType targetType, ResolvableType collectionType) {
            this.joinMethod = joinMethod;
            this.nestedJoinerAccessor = nestedJoinerAccessor;
            this.targetType = targetType;
            this.collectionType = collectionType;
        }

        boolean isToManyAssociation() {
            return collectionType != null;
        }

    }

}