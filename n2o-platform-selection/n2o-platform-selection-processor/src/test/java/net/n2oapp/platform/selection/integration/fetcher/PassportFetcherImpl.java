package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.Passport;
import net.n2oapp.platform.selection.integration.model.PassportFetcher;
import net.n2oapp.platform.selection.integration.model.PassportSelection;
import org.springframework.lang.NonNull;

public class PassportFetcherImpl extends BaseModelFetcherImpl<Passport, PassportSelection> implements PassportFetcher<Passport> {

    public PassportFetcherImpl(Passport src) {
        super(src);
    }

    @Override
    public @NonNull Passport create() {
        return new Passport();
    }

    @Override
    public String fetchSeries() {
        return src.getSeries();
    }

    @Override
    public String fetchNumber() {
        return src.getNumber();
    }

}
