package net.n2oapp.platform.selection.core.joiner;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.core.JoinUtil;
import net.n2oapp.platform.selection.core.domain.Address;
import net.n2oapp.platform.selection.core.domain.BaseModel;
import net.n2oapp.platform.selection.core.domain.Organisation;
import net.n2oapp.platform.selection.core.domain.OrganisationJoiner;
import net.n2oapp.platform.selection.core.fetcher.AddressFetcherImpl;
import net.n2oapp.platform.selection.core.fetcher.OrganisationFetcherImpl;
import net.n2oapp.platform.selection.core.repository.AddressRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

import static net.n2oapp.platform.selection.core.Application.mapNullable;

@Component
public class OrganisationJoinerImpl implements OrganisationJoiner<Integer, Organisation, OrganisationFetcherImpl> {

    private final AddressRepository addressRepository;

    public OrganisationJoinerImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public @NonNull Integer getId(Organisation entity) {
        return entity.getId();
    }

    @Override
    public @NonNull Organisation getUnderlyingEntity(OrganisationFetcherImpl fetcher) {
        return fetcher.getSource();
    }

    @Override
    public Map<Integer, Fetcher<Address>> joinLegalAddress(Collection<Organisation> organisations) {
        return JoinUtil.joinToOne(
            organisations,
            addressRepository::findLegalAddressesOfOrganisations,
            AddressFetcherImpl::new,
            Organisation::getId,
            organisation -> mapNullable(organisation.getLegalAddress(), BaseModel::getId),
            Address::getId
        );
    }

    @Override
    public Map<Integer, Fetcher<Address>> joinFactualAddress(Collection<Organisation> organisations) {
        return JoinUtil.joinToOne(
            organisations,
            addressRepository::findFactualAddressesOfOrganisations,
            AddressFetcherImpl::new,
            Organisation::getId,
            organisation -> mapNullable(organisation.getFactualAddress(), BaseModel::getId),
            Address::getId
        );
    }

}
