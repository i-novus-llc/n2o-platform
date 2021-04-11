package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Joined;
import net.n2oapp.platform.selection.api.Selective;

import javax.validation.constraints.NotNull;
import java.util.List;

@Selective
public class Model<M extends BaseModel, E extends M> extends BaseModel {

    @Joined
    private List<? extends M> model;

    @Joined
    private E model2;

    @Joined(withNestedJoiner = false)
    private Long aLong;

    @Joined(withNestedJoiner = false)
    @NotNull
    private @NotNull List<@NotNull Long> longs;

    public List<? extends M> getModel() {
        return model;
    }

    public void setModel(List<? extends M> model) {
        this.model = model;
    }

    public E getModel2() {
        return model2;
    }

    public void setModel2(E model2) {
        this.model2 = model2;
    }

    public Long getALong() {
        return aLong;
    }

    public void setALong(Long aLong) {
        this.aLong = aLong;
    }

    public void setLongs(List<Long> longs) {
        this.longs = longs;
    }

}
