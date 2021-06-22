package net.n2oapp.platform.selection.integration.joiner;

import net.n2oapp.platform.selection.api.JoinUtil;
import net.n2oapp.platform.selection.integration.fetcher.ContactFetcherImpl;
import net.n2oapp.platform.selection.integration.fetcher.OrganisationFetcherImpl;
import net.n2oapp.platform.selection.integration.fetcher.PassportFetcherImpl;
import net.n2oapp.platform.selection.integration.fetcher.ProjectFetcherImpl;
import net.n2oapp.platform.selection.integration.model.*;
import net.n2oapp.platform.selection.integration.repository.EmployeeRepository;
import net.n2oapp.platform.selection.integration.repository.OrganisationRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.n2oapp.platform.selection.unit.Util.mapNullable;

@Component
public class EmployeeJoinerImpl implements EmployeeJoiner<Employee, Integer> {

    private final EmployeeRepository employeeRepository;
    private final OrganisationRepository organisationRepository;
    private final OrganisationJoiner<Organisation, Integer> organisationJoiner;

    public EmployeeJoinerImpl(
        EmployeeRepository employeeRepository,
        OrganisationRepository organisationRepository,
        OrganisationJoiner<Organisation, Integer> organisationJoiner
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
    public Map<Integer, OrganisationFetcher<?>> joinOrganisation(List<Employee> employees, List<Integer> ids) {
        return JoinUtil.joinToOnePrefetching(
            employees,
            () -> organisationRepository.joinOrganisation(ids),
            OrganisationFetcherImpl::new,
            Employee::getOrganisation,
            Employee::getId
        );
    }

    @Override
    public OrganisationJoiner<Organisation, Integer> organisationJoiner() {
        return organisationJoiner;
    }

    @Override
    public Map<Integer, List<ContactFetcher<?>>> joinContacts(List<Employee> employees, List<Integer> ids) {
        return JoinUtil.joinOneToMany(
            () -> employeeRepository.joinContacts(ids),
            ContactFetcherImpl::new,
            contact -> contact.getOwner().getId()
        );
    }

    @Override
    public Map<Integer, Set<ProjectFetcher<?>>> joinProjects(List<Employee> employees, List<Integer> ids) {
        return JoinUtil.joinToMany(
            () -> employeeRepository.joinProjects(ids),
            ProjectFetcherImpl::new,
            Employee::getId,
            Employee::getProjects,
            HashSet::new
        );
    }

    @Override
    public Map<Integer, PassportFetcher<?>> joinPassport(List<Employee> employees, List<Integer> ids) {
        return JoinUtil.joinToOne(
            employees,
            () -> employeeRepository.joinPassport(ids),
            PassportFetcherImpl::new,
            Employee::getId,
            employee -> mapNullable(employee.getPassport(), BaseModel::getId),
            Passport::getId
        );
    }

}
