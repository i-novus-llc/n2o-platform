package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.Passport;
import net.n2oapp.platform.selection.core.domain.PassportFetcher;

public class PassportFetcherImpl extends BaseModelFetcherImpl<Passport> implements PassportFetcher {

    public PassportFetcherImpl(Passport src) {
        super(src);
    }

    @Override
    public Passport create() {
        return new Passport();
    }

    @Override
    public void fetchId(Passport model) {
        model.setId(src.getId());
    }

    @Override
    public void fetchSeries(Passport model) {
        model.setSeries(src.getSeries());
    }

    @Override
    public void fetchNumber(Passport model) {
        model.setNumber(src.getNumber());
    }

}
