package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Selective;

@Selective
public class BModel<B extends Model<B, B>> extends Model<B, B> {

    B b;

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    @Selective
    static class BBBModel extends BModel<BBBModel> {

    }

}
