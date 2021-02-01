package net.n2oapp.platform.selection.core.impl;

import net.n2oapp.platform.selection.core.Selector;
import net.n2oapp.platform.selection.core.domain.Employee;
import net.n2oapp.platform.selection.core.domain.EmployeeJoiner;
import net.n2oapp.platform.selection.core.fetcher.EmployeeFetcherImpl;
import net.n2oapp.platform.selection.core.repository.EmployeeRepository;
import net.n2oapp.platform.selection.core.rest.EmployeeCriteria;
import net.n2oapp.platform.selection.core.rest.SelectiveRest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SelectiveRestImpl implements SelectiveRest {

    private final EmployeeRepository employeeRepository;
    private final EmployeeJoiner<Integer, Employee, EmployeeFetcherImpl> joiner;

    public SelectiveRestImpl(EmployeeRepository employeeRepository, EmployeeJoiner<Integer, Employee, EmployeeFetcherImpl> joiner) {
        this.employeeRepository = employeeRepository;
        this.joiner = joiner;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> search(EmployeeCriteria criteria) {
        return Selector.resolvePage(joiner, employeeRepository.findAll(criteria).map(EmployeeFetcherImpl::new), criteria.selection());
    }

}
