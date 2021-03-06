package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query("SELECT a FROM Address a WHERE a.id IN (SELECT o.legalAddress.id FROM Organisation o WHERE o.id IN (?1))")
    List<Address> findLegalAddressesOfOrganisations(Collection<Integer> orgIds);

    @Query("SELECT a FROM Address a WHERE a.id IN (SELECT o.factualAddress.id FROM Organisation o WHERE o.id IN (?1))")
    List<Address> findFactualAddressesOfOrganisations(Collection<Integer> orgIds);

}
