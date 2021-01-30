package net.n2oapp.platform.selection.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {

    @Query("SELECT o FROM Organisation o WHERE o.id IN (SELECT e.organisation.id FROM Employee e WHERE e IN (?1))")
    List<Organisation> joinOrganisation(Collection<Employee> employees);

}
