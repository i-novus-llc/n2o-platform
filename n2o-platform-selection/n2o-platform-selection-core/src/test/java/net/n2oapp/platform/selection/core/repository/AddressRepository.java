package net.n2oapp.platform.selection.core.repository;

import net.n2oapp.platform.selection.core.domain.Address;
import net.n2oapp.platform.selection.core.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query("SELECT a FROM Address a WHERE a.id IN (SELECT o.legalAddress.id FROM Organisation o WHERE o IN (?1))")
    List<Address> findLegalAddressesOfOrganisations(Collection<Organisation> orgIds);

    @Query("SELECT a FROM Address a WHERE a.id IN (SELECT o.factualAddress.id FROM Organisation o WHERE o IN (?1))")
    List<Address> findFactualAddressesOfOrganisations(Collection<Organisation> orgIds);

}
