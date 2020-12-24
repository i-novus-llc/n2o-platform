package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor9;
import javax.lang.model.util.Types;
import java.util.List;

class GenericSignatureExtractor extends SimpleTypeVisitor9<GenericSignature, GenericSignature> {

    private final Types types;

    GenericSignatureExtractor(Types types) {
        this.types = types;
    }

    @Override
    public GenericSignature visitDeclared(DeclaredType t, GenericSignature signature) {
        if (signature.isImportsOnly()) {
            if (types.isSameType(types.erasure(t), types.erasure(signature.getOwner().asType()))) {
                signature.markLastSame();
            } else {
                signature.appendImport(t.asElement().toString());
            }
        } else {
            signature.setImportsOnly();
            if (!t.getTypeArguments().isEmpty()) {
                for (TypeMirror arg : t.getTypeArguments()) {
                    TypeVariable var = (TypeVariable) arg;
                    TypeMirror bound = var.getUpperBound();
                    signature.addTypeVariable(var.toString(), bound.toString());
                    bound.accept(this, signature);
                }
            }
        }
        return signature;
    }

    @Override
    public GenericSignature visitIntersection(IntersectionType t, GenericSignature signature) {
        List<? extends TypeMirror> bounds = t.getBounds();
        for (TypeMirror bound : bounds) {
            bound.accept(this, signature);
        }
        return signature;
    }

}
