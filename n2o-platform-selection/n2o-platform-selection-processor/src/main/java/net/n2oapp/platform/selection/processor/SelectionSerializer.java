package net.n2oapp.platform.selection.processor;

import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;

class SelectionSerializer extends AbstractSerializer {

    private final TypeMirror selectionEnum;
    private final TypeMirror selectionInterface;

    SelectionSerializer(TypeMirror selectionKey, TypeMirror selectionEnum, TypeMirror selectionInterface) {
        super(selectionKey);
        this.selectionEnum = selectionEnum;
        this.selectionInterface = selectionInterface;
    }

    @Override
    String getSuffix() {
        return "Selection";
    }

    @Override
    void serializeProperty(SelectionMeta meta, SelectionProperty property, Writer out) throws IOException {
        out.append(selectionEnum.toString()).append(' ');
        out.append("select").append(capitalize(property.getKey())).append("();");
        if (property.getNestedSelection() != null) {
            out.append("\n\t");
            appendSelectionKey(out, property.getKey());
            out.append("\n\t");
            SelectionMeta nestedSelection = property.getNestedSelection();
            out.append(getQualifiedName(nestedSelection, nestedSelection.getTargetPackage()));
            out.append(property.getNestedGenericSignature()).append(' ');
            out.append(property.getKey()).append(getSuffix()).append("();");
        }
    }

    @Override
    TypeMirror getInterfaceRaw() {
        return selectionInterface;
    }

}
