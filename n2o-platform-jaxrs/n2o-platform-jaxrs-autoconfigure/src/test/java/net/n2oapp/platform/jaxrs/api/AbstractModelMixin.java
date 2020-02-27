package net.n2oapp.platform.jaxrs.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringModel.class, name = "str"),
    @JsonSubTypes.Type(value = IntegerModel.class, name = "i32"),
    @JsonSubTypes.Type(value = ListModel.class, name = "list"),
})
public interface AbstractModelMixin {

}
