package net.n2oapp.platform.selection.integration.repository;

import net.n2oapp.platform.selection.integration.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Set;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("SELECT e FROM Employee e JOIN FETCH e.projects WHERE e.id IN (?1)")
    Set<Employee> joinProjects(Collection<Integer> workers);

}
