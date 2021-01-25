package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;

class FetcherSerializer extends AbstractSerializer {

    private final TypeMirror fetcherInterface;

    FetcherSerializer(TypeMirror selectionKey, TypeMirror fetcherInterface) {
        super(selectionKey);
        this.fetcherInterface = fetcherInterface;
    }

    @Override
    String getSuffix() {
        return "Fetcher";
    }

    @Override
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append("void ");
        out.append("select");
        out.append(capitalize(property.getKey()));
        out.append("(");
        out.append(meta.getFetcherTarget());
        out.append(" ");
        out.append("model");
        if (property.getNestedSelection() != null) {
            out.append(", ");
            out.append(property.getOriginalType());
            out.append(' ');
            out.append(property.getKey());
        }
        out.append(");");
        if (property.getNestedSelection() != null) {
            out.append("\n\t");
            appendSelectionKey(out, property.getKey());
            out.append("\n\t");
            if (property.getCollectionRawType() != null) {
                out.append(property.getCollectionRawType().toString());
                out.append("<? extends ");
            }
            out.append(getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage()));
            out.append(property.getNestedGenericSignature());
            if (property.getCollectionRawType() != null)
                out.append('>');
            out.append(' ');
            out.append(property.getKey());
            out.append(getSuffix());
            out.append("();");
        }
    }

    @Override
    TypeMirror getInterfaceRaw() {
        return fetcherInterface;
    }

}
