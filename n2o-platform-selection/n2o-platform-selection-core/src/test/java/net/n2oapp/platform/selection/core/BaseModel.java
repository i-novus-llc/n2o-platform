package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Selective;

@Selective
public class BaseModel {

    public Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
