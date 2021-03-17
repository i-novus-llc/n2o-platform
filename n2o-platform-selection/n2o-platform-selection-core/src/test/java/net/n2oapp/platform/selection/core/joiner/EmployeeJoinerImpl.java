package net.n2oapp.platform.selection.core.joiner;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.core.JoinUtil;
import net.n2oapp.platform.selection.core.domain.*;
import net.n2oapp.platform.selection.core.fetcher.*;
import net.n2oapp.platform.selection.core.repository.EmployeeRepository;
import net.n2oapp.platform.selection.core.repository.OrganisationRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static net.n2oapp.platform.selection.core.Application.mapNullable;

@Component
public class EmployeeJoinerImpl implements EmployeeJoiner<Integer, Employee, EmployeeFetcherImpl> {

    private final EmployeeRepository employeeRepository;
    private final OrganisationRepository organisationRepository;
    private final OrganisationJoiner<Integer, Organisation, OrganisationFetcherImpl> organisationJoiner;

    public EmployeeJoinerImpl(
        EmployeeRepository employeeRepository,
        OrganisationRepository organisationRepository,
        OrganisationJoiner<Integer, Organisation, OrganisationFetcherImpl> organisationJoiner
    ) {
        this.employeeRepository = employeeRepository;
        this.organisationRepository = organisationRepository;
        this.organisationJoiner = organisationJoiner;
    }

    @Override
    public @NonNull
    Integer getId(Employee entity) {
        return entity.getId();
    }

    @Override
    public @NonNull
    Employee getUnderlyingEntity(EmployeeFetcherImpl fetcher) {
        return fetcher.getSource();
    }

    @Override
    public Map<Integer, Fetcher<Organisation>> joinOrganisation(Collection<Employee> employees) {
        return JoinUtil.joinToOnePrefetching(
            employees,
            organisationRepository::joinOrganisation,
            OrganisationFetcherImpl::new,
            Employee::getOrganisation,
            Employee::getId
        );
    }

    @Override
    public OrganisationJoiner<Integer, Organisation, OrganisationFetcherImpl> organisationJoiner() {
        return organisationJoiner;
    }

    @Override
    public Map<Integer, List<Fetcher<Contact>>> joinCntcts(Collection<Employee> employees) {
        return JoinUtil.joinOneToMany(
            employees,
            employeeRepository::joinContacts,
            ContactFetcherImpl::new,
            contact -> contact.getOwner().getId()
        );
    }

    @Override
    public Map<Integer, Set<Fetcher<Project>>> joinProjects(Collection<Employee> employees) {
        return JoinUtil.joinToMany(
            employees,
            employeeRepository::joinProjects,
            ProjectFetcherImpl::new,
            Employee::getId,
            Employee::getProjects,
            HashSet::new
        );
    }

    @Override
    public Map<Integer, Fetcher<Passport>> joinPassport(Collection<Employee> entities) {
        return JoinUtil.joinToOne(
            entities,
            employeeRepository::joinPassport,
            PassportFetcherImpl::new,
            Employee::getId,
            employee -> mapNullable(employee.getPassport(), BaseModel::getId),
            Passport::getId
        );
    }

}
