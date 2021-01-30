package net.n2oapp.platform.selection.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.owner IN (?1)")
    List<Contact> joinContacts(Iterable<Employee> owners);

}
