package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.Address;
import net.n2oapp.platform.selection.core.domain.AddressFetcher;

public class AddressFetcherImpl extends BaseModelFetcherImpl<Address> implements AddressFetcher {

    public AddressFetcherImpl(Address src) {
        super(src);
    }

    @Override
    public Address create() {
        return new Address();
    }

    @Override
    public void fetchPostcode(Address model) {
        model.setPostcode(src.getPostcode());
    }

    @Override
    public void fetchRegion(Address model) {
        model.setRegion(src.getRegion());
    }

}
