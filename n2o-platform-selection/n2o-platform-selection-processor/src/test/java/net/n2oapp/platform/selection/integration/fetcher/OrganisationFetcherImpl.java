package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.*;
import org.springframework.lang.NonNull;

import static net.n2oapp.platform.selection.unit.Util.mapNullable;

public class OrganisationFetcherImpl extends BaseModelFetcherImpl<Organisation, OrganisationSelection> implements OrganisationFetcher<Organisation> {

    public OrganisationFetcherImpl(Organisation src) {
        super(src);
    }

    @Override
    public @NonNull Organisation create() {
        return new Organisation();
    }

    @Override
    public AddressFetcher<Address> fetchLegalAddress() {
        return mapNullable(src.getLegalAddress(), AddressFetcherImpl::new);
    }

    @Override
    public AddressFetcher<Address> fetchFactualAddress() {
        return mapNullable(src.getFactualAddress(), AddressFetcherImpl::new);
    }

    @Override
    public String fetchName() {
        return src.getName();
    }

}
