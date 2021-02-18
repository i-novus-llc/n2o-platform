package net.n2oapp.platform.selection.core.fetcher;

import net.n2oapp.platform.selection.core.domain.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static net.n2oapp.platform.selection.core.Application.mapNullable;

public class EmployeeFetcherImpl extends BaseModelFetcherImpl<Employee> implements EmployeeFetcher {

    public EmployeeFetcherImpl(Employee src) {
        super(src);
    }

    @Override
    public Employee create() {
        return new Employee();
    }

    @Override
    public void fetchName(Employee model) {
        model.setName(src.getName());
    }

    @Override
    public void setOrganisation(Employee model, Organisation organisation) {
        model.setOrganisation(organisation);
    }

    @Override
    public OrganisationFetcher organisationFetcher() {
        return mapNullable(src.getOrganisation(), OrganisationFetcherImpl::new);
    }

    @Override
    public void setContacts(Employee model, List<Contact> contacts) {
        model.setContacts(contacts);
    }

    @Override
    public List<? extends ContactFetcher> contactsFetcher() {
        return src.getContacts().stream().map(ContactFetcherImpl::new).collect(toList());
    }

    @Override
    public void setProjects(Employee model, Set<Project> projects) {
        model.setProjects(projects);
    }

    @Override
    public Set<? extends ProjectFetcher> projectsFetcher() {
        return src.getProjects().stream().map(ProjectFetcherImpl::new).collect(Collectors.toSet());
    }

    @Override
    public void setPassport(Employee model, Passport passport) {
        model.setPassport(passport);
    }

    @Override
    public PassportFetcher passportFetcher() {
        return new PassportFetcherImpl(src.getPassport());
    }

}
