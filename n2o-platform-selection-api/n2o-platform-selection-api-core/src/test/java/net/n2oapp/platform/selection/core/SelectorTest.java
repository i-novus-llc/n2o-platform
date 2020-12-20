package net.n2oapp.platform.selection.core;

import org.junit.Assert;
import org.junit.Test;

public class SelectorTest {

    @Test
    public void testResolve() {
        EmployeeEntity entity = new EmployeeEntity("Alec", "Balduin");
        EmployeeSelection selection = new EmployeeSelection();
        selection.setSelectName(SelectionEnum.T);
        selection.setSelectSurname(SelectionEnum.F);
        EmployeeDto employee = Selector.resolve(entity, selection);
        Assert.assertNotNull(employee.getName());
        Assert.assertNull(employee.getSurname());
    }

}
