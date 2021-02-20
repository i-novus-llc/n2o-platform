package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.Address;
import net.n2oapp.platform.selection.core.domain.AddressFetcher;
import net.n2oapp.platform.selection.core.domain.Organisation;
import net.n2oapp.platform.selection.core.domain.OrganisationFetcher;

import static net.n2oapp.platform.selection.core.Application.mapNullable;

public class OrganisationFetcherImpl extends BaseModelFetcherImpl<Organisation> implements OrganisationFetcher {

    public OrganisationFetcherImpl(Organisation src) {
        super(src);
    }

    @Override
    public Organisation create() {
        return new Organisation();
    }

    @Override
    public void setLegalAddress(Organisation model, Address legalAddress) {
        model.setLegalAddress(legalAddress);
    }

    @Override
    public AddressFetcher legalAddressFetcher() {
        return mapNullable(src.getLegalAddress(), AddressFetcherImpl::new);
    }

    @Override
    public void setFactualAddress(Organisation model, Address factualAddress) {
        model.setFactualAddress(factualAddress);
    }

    @Override
    public AddressFetcher factualAddressFetcher() {
        return mapNullable(src.getFactualAddress(), AddressFetcherImpl::new);
    }

    @Override
    public void fetchName(Organisation model) {
        model.setName(src.getName());
    }

}
