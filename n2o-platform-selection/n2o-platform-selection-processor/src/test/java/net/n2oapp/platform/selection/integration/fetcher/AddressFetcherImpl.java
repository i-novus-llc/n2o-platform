package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.Address;
import net.n2oapp.platform.selection.integration.model.AddressFetcher;
import net.n2oapp.platform.selection.integration.model.AddressSelection;
import org.springframework.lang.NonNull;

public class AddressFetcherImpl extends BaseModelFetcherImpl<Address, AddressSelection> implements AddressFetcher<Address> {

    public AddressFetcherImpl(Address src) {
        super(src);
    }

    @Override
    public @NonNull Address create() {
        return new Address();
    }

    @Override
    public String fetchPostcode() {
        return src.getPostcode();
    }

    @Override
    public String fetchRegion() {
        return src.getRegion();
    }

}
