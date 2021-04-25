package net.n2oapp.platform.selection.integration.joiner;

import net.n2oapp.platform.selection.api.JoinUtil;
import net.n2oapp.platform.selection.integration.fetcher.AddressFetcherImpl;
import net.n2oapp.platform.selection.integration.model.*;
import net.n2oapp.platform.selection.integration.repository.AddressRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

import static net.n2oapp.platform.selection.unit.Util.mapNullable;

@Component
public class OrganisationJoinerImpl implements OrganisationJoiner<Organisation, Integer> {

    private final AddressRepository addressRepository;

    public OrganisationJoinerImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public @NonNull Integer getId(Organisation entity) {
        return entity.getId();
    }

    @Override
    public Map<Integer, AddressFetcher<?>> joinLegalAddress(Collection<Organisation> organisations) {
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
    public Map<Integer, AddressFetcher<?>> joinFactualAddress(Collection<Organisation> organisations) {
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
