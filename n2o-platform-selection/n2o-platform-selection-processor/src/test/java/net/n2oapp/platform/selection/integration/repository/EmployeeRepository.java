package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Contact;
import net.n2oapp.platform.selection.integration.model.Employee;
import net.n2oapp.platform.selection.integration.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.owner.id IN (?1)")
    List<Contact> joinContacts(Collection<Integer> owners);

    @Query("SELECT e FROM Employee e JOIN FETCH e.projects WHERE e.id IN (?1)")
    Set<Employee> joinProjects(Collection<Integer> workers);

    @Query("SELECT p FROM Passport p WHERE p IN (SELECT e.passport FROM Employee e WHERE e.id IN (?1))")
    List<Passport> joinPassport(List<Integer> employees);

}
