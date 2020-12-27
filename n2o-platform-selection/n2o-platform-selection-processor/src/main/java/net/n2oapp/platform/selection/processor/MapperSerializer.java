package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;

class MapperSerializer extends AbstractSerializer {

    private final TypeMirror mapperInterface;

    MapperSerializer(TypeMirror selectionKey, TypeMirror mapperInterface) {
        super(selectionKey);
        this.mapperInterface = mapperInterface;
    }

    @Override
    String getSuffix() {
        return "Mapper";
    }

    @Override
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append("void ").append("select").append(capitalize(property.getKey())).append("(");
        out.append(meta.getMapperTarget()).append(" ").append("model");
        if (property.getNestedSelection() != null) {
            out.append(", ");
            out.append(property.getOriginalType().toString()).append(' ');
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
            out.append(' ').append(property.getKey()).append(getSuffix()).append("();");
        }
    }

    @Override
    TypeMirror getInterfaceRaw() {
        return mapperInterface;
    }

}
