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

        }
        out.append(");");
    }

    @Override
    TypeMirror getInterfaceRaw() {
        return mapperInterface;
    }

}
