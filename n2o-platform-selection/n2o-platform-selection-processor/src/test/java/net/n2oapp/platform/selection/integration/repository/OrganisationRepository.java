package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {

    @Query("SELECT o FROM Organisation o WHERE o.id IN (SELECT e.organisation.id FROM Employee e WHERE e.id IN (?1))")
    List<Organisation> joinOrganisation(Collection<Integer> employees);

}
