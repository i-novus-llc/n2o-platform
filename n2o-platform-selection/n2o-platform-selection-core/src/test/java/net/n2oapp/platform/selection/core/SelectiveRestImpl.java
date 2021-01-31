package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.JoinUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class SelectiveRestImpl implements SelectiveRest {

    private final EmployeeRepository employeeRepository;
    private final OrganisationRepository organisationRepository;
    private final AddressRepository addressRepository;

    public SelectiveRestImpl(EmployeeRepository employeeRepository, OrganisationRepository organisationRepository, AddressRepository addressRepository) {
        this.employeeRepository = employeeRepository;
        this.organisationRepository = organisationRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> search(EmployeeCriteria criteria) {
        return Selector.resolvePage(new EmployeeJoinerImpl(), employeeRepository.findAll(criteria).map(EmployeeFetcherImpl::new), criteria.selection());
    }

    public class EmployeeJoinerImpl implements EmployeeJoiner<Integer, Employee, EmployeeFetcherImpl> {

        @Override
        public Map<Integer, Fetcher<Organisation>> joinOrganisation(Collection<Employee> employees) {
            return JoinUtil.joinUnidirectionalToOnePrefetching(
                    employees,
                    organisationRepository::joinOrganisation,
                    OrganisationFetcherImpl::new,
                    Employee::getOrganisation,
                    Employee::getId
            );
        }

        @Override
        public OrganisationJoinerImpl organisationJoiner() {
            return new OrganisationJoinerImpl();
        }

        @Override
        public Map<Integer, List<Fetcher<Contact>>> joinContacts(Collection<Employee> employees) {
            return JoinUtil.joinToMany(
                employees,
                employeeRepository::joinContacts,
                EmployeeFetcherImpl.ContactFetcherImpl::new,
                Employee::getId,
                Employee::getContacts
            );
        }

        @Override
        public Map<Integer, Set<Fetcher<Project>>> joinProjects(Collection<Employee> employees) {
            return JoinUtil.joinToMany(
                employees,
                employeeRepository::joinProjects,
                EmployeeFetcherImpl.ProjectFetcherImpl::new,
                Employee::getId,
                Employee::getProjects,
                HashSet::new
            );
        }

        @Override
        public Integer getId(Employee entity) {
            return entity.getId();
        }

        @Override
        public Employee getUnderlyingEntity(EmployeeFetcherImpl fetcher) {
            return fetcher.src;
        }

    }

    public class OrganisationJoinerImpl implements OrganisationJoiner<Integer, Organisation, OrganisationFetcherImpl> {

        @Override
        public Map<Integer, Fetcher<Address>> joinLegalAddress(Collection<Organisation> organisations) {
            return JoinUtil.joinUnidirectionalToOne(
                    organisations,
                    addressRepository::findLegalAddressesOfOrganisations,
                    AddressFetcherImpl::new,
                    Organisation::getId,
                    organisation -> organisation.getLegalAddress() == null ? null : organisation.getLegalAddress().getId(),
                    Address::getId
            );
        }

        @Override
        public Map<Integer, Fetcher<Address>> joinFactualAddress(Collection<Organisation> organisations) {
            return JoinUtil.joinUnidirectionalToOne(
                    organisations,
                    addressRepository::findFactualAddressesOfOrganisations,
                    AddressFetcherImpl::new,
                    Organisation::getId,
                    organisation -> organisation.getFactualAddress() == null ? null : organisation.getFactualAddress().getId(),
                    Address::getId
            );
        }

        @Override
        public Integer getId(Organisation entity) {
            return entity.getId();
        }

        @Override
        public Organisation getUnderlyingEntity(OrganisationFetcherImpl fetcher) {
            return fetcher.src;
        }

    }

    private static class EmployeeFetcherImpl implements EmployeeFetcher {

        private final Employee src;

        private EmployeeFetcherImpl(Employee src) {
            this.src = src;
        }

        @Override
        public Employee create() {
            return new Employee();
        }

        @Override
        public void fetchId(Employee model) {
            model.setId(src.getId());
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
            return src.getOrganisation() == null ? null : new OrganisationFetcherImpl(src.getOrganisation());
        }

        @Override
        public void setContacts(Employee model, List<Contact> contacts) {
            model.setContacts(contacts);
        }

        @Override
        public List<? extends ContactFetcher> contactsFetcher() {
            return src.getContacts() == null ? null : src.getContacts().stream().map(ContactFetcherImpl::new).collect(toList());
        }

        @Override
        public void setProjects(Employee model, Set<Project> projects) {
            model.setProjects(projects);
        }

        @Override
        public Set<? extends ProjectFetcher> projectsFetcher() {
            return src.getProjects().stream().map(ProjectFetcherImpl::new).collect(Collectors.toSet());
        }

        private static class ContactFetcherImpl implements ContactFetcher, Fetcher<Contact> {

            private final Contact contact;

            public ContactFetcherImpl(Contact contact) {
                this.contact = contact;
            }

            @Override
            public void fetchPhone(Contact model) {
                model.setPhone(contact.getPhone());
            }

            @Override
            public void fetchEmail(Contact model) {
                model.setEmail(contact.getEmail());
            }

            @Override
            public Contact create() {
                return new Contact();
            }

            @Override
            public void fetchId(Contact model) {
                model.setId(contact.getId());
            }

        }

        private static class ProjectFetcherImpl implements ProjectFetcher {

            private final Project project;

            public ProjectFetcherImpl(Project project) {
                this.project = project;
            }

            @Override
            public void setWorkers(Project model, Set<Employee> workers) {
            }

            @Override
            public Set<? extends EmployeeFetcher> workersFetcher() {
                return null;
            }

            @Override
            public void fetchName(Project model) {
                model.setName(project.getName());
            }

            @Override
            public void fetchId(Project model) {
                model.setId(project.getId());
            }

            @Override
            public Project create() {
                return new Project();
            }

        }
    }

    private static class OrganisationFetcherImpl implements OrganisationFetcher {

        private final Organisation src;

        private OrganisationFetcherImpl(Organisation src) {
            this.src = src;
        }

        @Override
        public Organisation create() {
            return new Organisation();
        }

        @Override
        public void fetchId(Organisation model) {
            model.setId(src.getId());
        }

        @Override
        public void setLegalAddress(Organisation model, Address legalAddress) {
            model.setLegalAddress(legalAddress);
        }

        @Override
        public AddressFetcher legalAddressFetcher() {
            return src.getLegalAddress() == null ? null : new AddressFetcherImpl(src.getLegalAddress());
        }

        @Override
        public void setFactualAddress(Organisation model, Address factualAddress) {
            model.setFactualAddress(factualAddress);
        }

        @Override
        public AddressFetcher factualAddressFetcher() {
            return src.getFactualAddress() == null ? null : new AddressFetcherImpl(src.getFactualAddress());
        }

        @Override
        public void fetchName(Organisation model) {
            model.setName(src.getName());
        }

    }

    private static class AddressFetcherImpl implements AddressFetcher {

        private final Address src;

        private AddressFetcherImpl(Address src) {
            this.src = src;
        }

        @Override
        public Address create() {
            return new Address();
        }

        @Override
        public void fetchPostcode(Address model) {
            model.setPostcode(src.getPostcode());
        }

        @Override
        public void fetchRegion(Address model) {
            model.setRegion(src.getRegion());
        }

        @Override
        public void fetchId(Address model) {
            model.setId(src.getId());
        }

    }

}
