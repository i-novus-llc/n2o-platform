package net.n2oapp.platform.selection.unit;

import net.n2oapp.platform.selection.integration.fetcher.EmployeeFetcherImpl;
import net.n2oapp.platform.selection.integration.joiner.EmployeeJoinerImpl;
import net.n2oapp.platform.selection.integration.model.Employee;
import net.n2oapp.platform.selection.integration.model.EmployeeSelection;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinerTest {

    @Test
    public void test() {
        EmployeeJoinerImpl joiner = new EmployeeJoinerImpl(
            null,
            null,
            null
        );
        final Employee emp = new Employee();
        emp.setId(1);
        final List<Employee> list = joiner.resolveCollection(
            Collections.singletonList(new EmployeeFetcherImpl(emp)),
            EmployeeSelection.create().id()
        );
        assertThat(list).isNotNull().hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(emp.getId());
        final HashSet<Employee> set = joiner.resolveCollection(
            Collections.singletonList(new EmployeeFetcherImpl(emp)),
            EmployeeSelection.create().id(),
            HashSet::new
        );
        assertThat(set).isNotNull().hasSize(1);
        assertThat(set.iterator().next().getId()).isEqualTo(emp.getId());
        final Employee resolved = joiner.resolve(
            new EmployeeFetcherImpl(emp),
            EmployeeSelection.create().id()
        );
        assertThat(resolved).isNotNull();
        assertThat(resolved.getId()).isEqualTo(emp.getId());
    }

}
