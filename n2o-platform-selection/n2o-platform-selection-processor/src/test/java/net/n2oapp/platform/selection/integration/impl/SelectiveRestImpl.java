package net.n2oapp.platform.selection.integration.impl;

import net.n2oapp.platform.selection.integration.fetcher.EmployeeFetcherImpl;
import net.n2oapp.platform.selection.integration.model.Employee;
import net.n2oapp.platform.selection.integration.model.EmployeeJoiner;
import net.n2oapp.platform.selection.integration.repository.EmployeeRepository;
import net.n2oapp.platform.selection.integration.rest.EmployeeCriteria;
import net.n2oapp.platform.selection.integration.rest.SelectiveRest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SelectiveRestImpl implements SelectiveRest {

    private final EmployeeRepository employeeRepository;
    private final EmployeeJoiner<Employee, Integer> joiner;

    public SelectiveRestImpl(
        EmployeeRepository employeeRepository,
        EmployeeJoiner<Employee, Integer> joiner
    ) {
        this.employeeRepository = employeeRepository;
        this.joiner = joiner;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> search(EmployeeCriteria criteria) {
        return joiner.resolveStreamable(
            employeeRepository.findAll(criteria).map(EmployeeFetcherImpl::new),
            criteria.selection()
        );
    }

}
