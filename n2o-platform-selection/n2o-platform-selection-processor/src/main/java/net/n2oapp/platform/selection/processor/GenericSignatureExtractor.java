package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor9;

class GenericSignatureExtractor extends SimpleTypeVisitor9<GenericSignature, GenericSignature> {

    GenericSignatureExtractor() {
    }

    @Override
    public GenericSignature visitDeclared(DeclaredType t, GenericSignature signature) {
        if (!t.getTypeArguments().isEmpty()) {
            for (TypeMirror arg : t.getTypeArguments()) {
                TypeVariable var = (TypeVariable) arg;
                TypeMirror bound = var.getUpperBound();
                signature.addTypeVariable(var.toString(), bound.toString());
            }
        }
        return signature;
    }

}
