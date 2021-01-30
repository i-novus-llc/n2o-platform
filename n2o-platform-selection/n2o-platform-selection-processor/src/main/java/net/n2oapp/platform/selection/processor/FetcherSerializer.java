package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Fetcher;

import java.io.IOException;
import java.io.Writer;

class FetcherSerializer extends AbstractSerializer {

    @Override
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append("void ");
        out.append(property.getNestedSelection() == null ? "fetch" : "set");
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
            out.append(property.getNestedGenericSignatureOrWildcards());
            if (property.getCollectionRawType() != null)
                out.append('>');
            out.append(' ');
            out.append(property.getKey());
            out.append(getSuffix());
            out.append("();");
        }
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Fetcher.class;
    }

    @Override
    protected GenericSignature getGenericSignature(SelectionMeta meta) {
        return meta.getGenericSignature();
    }

    @Override
    protected String getExtendsSignature(SelectionMeta meta) {
        return meta.getExtendsSignature();
    }

}
