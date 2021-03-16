package net.n2oapp.platform.selection.processor;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.Joiner;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

class JoinerSerializer extends AbstractSerializer {

    @Override
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append(Map.class.getCanonicalName());
        out.append("<");
        out.append(meta.getIdTypeVariable());
        out.append(", ");
        if (property.getCollectionRawType() == null) {
            out.append(Fetcher.class.getCanonicalName());
            out.append("<");
            out.append(property.getType());
            out.append(">");
        } else {
            out.append(property.getCollectionRawType().toString());
            out.append("<");
            out.append(Fetcher.class.getCanonicalName());
            out.append("<");
            out.append(property.getType());
            out.append(">");
            out.append(">");
        }
        out.append(">");
        out.append(" ");
        out.append("join");
        out.append(capitalize(property.getKey()));
        out.append("(");
        out.append(Collection.class.getCanonicalName());
        out.append("<");
        out.append(meta.getEntityTypeVariable());
        out.append(">");
        out.append(" ");
        out.append("entities");
        out.append(");");
        if (property.isWithNestedJoiner()) {
            out.append("\n\t");
            appendSelectionKey(out, property.getKey());
            out.append("\n\t");
            out.append(getQualifiedName(property.getNestedSelection(), property.getNestedSelection().getTargetPackage()));
            out.append(property.getNestedGenericSignatureOrWildcards("?", "?", "?"));
            out.append(" ");
            out.append(property.getKey());
            out.append(getSuffix());
            out.append("();");
        }
    }

    @Override
    Class<?> getInterfaceRaw() {
        return Joiner.class;
    }

    @Override
    protected GenericSignature getGenericSignature(SelectionMeta meta) {
        return meta.getJoinerGenericSignature();
    }

    @Override
    protected String getExtendsSignature(SelectionMeta meta) {
        return meta.getJoinerExtendsSignature();
    }

    @Override
    protected boolean shouldSerialize(SelectionProperty property) {
        return property.isJoined();
    }

}
