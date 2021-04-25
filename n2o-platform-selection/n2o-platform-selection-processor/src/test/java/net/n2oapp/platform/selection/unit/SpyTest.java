package net.n2oapp.platform.selection.unit;

import net.n2oapp.platform.selection.api.SelectionPropagation;
import net.n2oapp.platform.selection.integration.model.ContactSelection;
import net.n2oapp.platform.selection.integration.model.Employee;
import net.n2oapp.platform.selection.integration.model.EmployeeSelection;
import net.n2oapp.platform.selection.integration.model.EmployeeSpy;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SpyTest {

    @Test
    public void test() {
        EmployeeSelection selection = EmployeeSelection.create().id();
        Employee employee = new Employee();
        EmployeeSpy spy = EmployeeSpy.spy(employee, selection);
        assertThatCode(spy::getId).doesNotThrowAnyException();
        assertThatThrownBy(spy::getContacts).hasMessageContaining("'contacts'");
        selection.contacts(ContactSelection.create().id());
        assertThatCode(spy::getContacts).doesNotThrowAnyException();
        selection.unselectId().unselectContacts().propagate(SelectionPropagation.ALL);
        spy = EmployeeSpy.spy(employee, selection);
        assertThatCode(spy::getId).doesNotThrowAnyException();
        assertThatThrownBy(spy::getContacts).hasMessageContaining("'contacts'");
        selection.propagate(SelectionPropagation.NESTED);
        spy = EmployeeSpy.spy(employee, selection);
        assertThatCode(spy::getId).doesNotThrowAnyException();
        assertThatCode(spy::getContacts).doesNotThrowAnyException();
        selection.propagate(SelectionPropagation.NORMAL).name();
        spy = EmployeeSpy.spy(employee, selection);
        assertThatThrownBy(spy::getId).hasMessageContaining("'id'");
    }

}
