package net.n2oapp.platform.selection.integration.model;

import net.n2oapp.platform.selection.api.Selective;

@Selective
public class AModel<A extends BaseModel, B extends A> extends Model<A, B> {
}
