package net.n2oapp.platform.selection.integration.joiner;

import net.n2oapp.platform.selection.api.JoinUtil;
import net.n2oapp.platform.selection.integration.fetcher.AddressFetcherImpl;
import net.n2oapp.platform.selection.integration.model.*;
import net.n2oapp.platform.selection.integration.repository.AddressRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public Map<Integer, AddressFetcher<?>> joinLegalAddress(List<Organisation> organisations, List<Integer> ids) {
        return JoinUtil.joinToOne(
            organisations,
            () -> addressRepository.findLegalAddressesOfOrganisations(ids),
            AddressFetcherImpl::new,
            Organisation::getId,
            organisation -> mapNullable(organisation.getLegalAddress(), BaseModel::getId),
            Address::getId
        );
    }

    @Override
    public Map<Integer, AddressFetcher<?>> joinFactualAddress(List<Organisation> organisations, List<Integer> ids) {
        return JoinUtil.joinToOne(
            organisations,
            () -> addressRepository.findFactualAddressesOfOrganisations(ids),
            AddressFetcherImpl::new,
            Organisation::getId,
            organisation -> mapNullable(organisation.getFactualAddress(), BaseModel::getId),
            Address::getId
        );
    }

}
