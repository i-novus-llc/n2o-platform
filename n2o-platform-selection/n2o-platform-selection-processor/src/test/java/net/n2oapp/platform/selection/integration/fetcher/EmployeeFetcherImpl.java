package net.n2oapp.platform.selection.integration.fetcher;

import net.n2oapp.platform.selection.integration.Util;
import net.n2oapp.platform.selection.integration.model.*;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.n2oapp.platform.selection.integration.Util.mapCollection;

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

    @Override
    public OrganisationFetcherImpl fetchOrganisation() {
        return Util.mapNullable(src.getOrganisation(), OrganisationFetcherImpl::new);
    }

    @Override
    public List<ContactFetcher<?>> fetchContacts() {
        return mapCollection(src.getContacts(), ContactFetcherImpl::new);
    }

    @Override
    public Set<ProjectFetcher<?>> fetchProjects() {
        return mapCollection(src.getProjects(), ProjectFetcherImpl::new, HashSet::new);
    }

    @Override
    public PassportFetcher<Passport> fetchPassport() {
        return Util.mapNullable(src.getPassport(), PassportFetcherImpl::new);
    }

}
