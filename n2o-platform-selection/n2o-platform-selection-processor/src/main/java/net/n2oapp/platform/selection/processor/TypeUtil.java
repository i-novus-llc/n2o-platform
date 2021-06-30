package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.List;

final class TypeUtil {

    private TypeUtil() {
        throw new UnsupportedOperationException();
    }

    static boolean containsTypeVariables(TypeMirror mirror) {
        if (mirror instanceof TypeVariable)
            return true;
        if (mirror instanceof WildcardType) {
            return containsTypeVariables(((WildcardType) mirror).getExtendsBound()) || containsTypeVariables(((WildcardType) mirror).getSuperBound());
        }
        if (mirror instanceof DeclaredType) {
            List<? extends TypeMirror> args = ((DeclaredType) mirror).getTypeArguments();
            return args.stream().anyMatch(TypeUtil::containsTypeVariables);
        }
        return false;
    }

}
