package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.model.Employee;
import net.n2oapp.platform.selection.integration.model.EmployeeFetcher;
import net.n2oapp.platform.selection.integration.model.EmployeeSelection;
import org.springframework.lang.NonNull;

public class EmployeeFetcherImpl extends BaseModelFetcherImpl<Employee, EmployeeSelection> implements EmployeeFetcher<Employee> {

    public EmployeeFetcherImpl(Employee src) {
        super(src);
    }

    @Override
    public @NonNull Employee create() {
        return new Employee();
    }

    @Override
    public String fetchName() {
        return src.getName();
    }

}
