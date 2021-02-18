package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.BaseModel;
import net.n2oapp.platform.selection.core.domain.BaseModelFetcher;

public abstract class BaseModelFetcherImpl<T extends BaseModel> implements BaseModelFetcher<T> {

    protected final T src;

    protected BaseModelFetcherImpl(T src) {
        this.src = src;
    }

    @Override
    public void fetchId(T model) {
        model.setId(src.getId());
    }

    public T getSource() {
        return src;
    }

}
