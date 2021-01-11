package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.jaxrs.RestCriteria;
import net.n2oapp.platform.selection.api.Selection;
import org.springframework.data.domain.Sort;

import javax.ws.rs.QueryParam;
import java.util.List;

public class EmployeeCriteria extends RestCriteria {

    @QueryParam("selection")
    private String selection;

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public void setSelection(DefaultEmployeeSelection selection) {
        this.selection = Selection.encode(selection);
    }

    public DefaultEmployeeSelection selection() {
        return Selection.decode(selection, DefaultEmployeeSelection.class);
    }

    @Override
    protected List<Sort.Order> getDefaultOrders() {
        return List.of(Sort.Order.asc("id"));
    }

}
