package net.n2oapp.platform.selection.integration.rest;

import net.n2oapp.platform.jaxrs.RestCriteria;
import net.n2oapp.platform.selection.api.Selection;
import net.n2oapp.platform.selection.integration.model.EmployeeSelection;
import org.springframework.data.domain.Sort;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.QueryParam;
import java.util.List;

public class EmployeeCriteria extends RestCriteria {

    @QueryParam("selection")
    private String selection;

    @BeanParam
    private EmployeeSelection employeeSelection;

    public EmployeeSelection getEmployeeSelection() {
        return employeeSelection;
    }

    public void setEmployeeSelection(EmployeeSelection employeeSelection) {
        this.employeeSelection = employeeSelection;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public void setSelection(EmployeeSelection selection) {
        this.selection = Selection.encode(selection);
    }

    public EmployeeSelection selection() {
        return Selection.decode(selection, EmployeeSelection.class);
    }

    @Override
    protected List<Sort.Order> getDefaultOrders() {
        return List.of(Sort.Order.asc("id"));
    }

}
