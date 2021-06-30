package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.BaseModel;
import net.n2oapp.platform.selection.integration.model.BaseModelFetcher;
import net.n2oapp.platform.selection.integration.model.BaseModelSelection;
import org.springframework.lang.NonNull;

public abstract class BaseModelFetcherImpl<T extends BaseModel, S extends BaseModelSelection<T>> implements BaseModelFetcher<T, S, T> {

    protected final T src;

    protected BaseModelFetcherImpl(T src) {
        this.src = src;
    }

    @Override
    public Integer fetchId() {
        return src.getId();
    }

    public T getSource() {
        return src;
    }

    @Override
    public @NonNull T getUnderlyingEntity() {
        return src;
    }

}
