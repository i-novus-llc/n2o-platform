package net.n2oapp.platform.selection.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("SELECT e FROM Employee e JOIN FETCH e.contacts WHERE e IN (?1)")
    Set<Employee> joinContacts(Collection<Employee> owners);

    @Query("SELECT e FROM Employee e JOIN FETCH e.projects WHERE e IN (?1)")
    Set<Employee> joinProjects(Collection<Employee> workers);

    @Query("SELECT p FROM Passport p WHERE p IN (SELECT e.passport FROM Employee e WHERE e IN (?1))")
    List<Passport> joinPassport(Collection<Employee> employees);

}
