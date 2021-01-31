package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.selection.api.Fetcher;
import net.n2oapp.platform.selection.api.JoinUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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
            return JoinUtil.joinToOnePrefetching(
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
                ContactFetcherImpl::new,
                Employee::getId,
                Employee::getContacts
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
            return JoinUtil.joinToOne(
                    organisations,
                    addressRepository::findLegalAddressesOfOrganisations,
                    AddressFetcherImpl::new,
                    Organisation::getId,
                    organisation -> mapNullable(organisation.getLegalAddress(), BaseModel::getId),
                    Address::getId
            );
        }

        @Override
        public Map<Integer, Fetcher<Address>> joinFactualAddress(Collection<Organisation> organisations) {
            return JoinUtil.joinToOne(
                    organisations,
                    addressRepository::findFactualAddressesOfOrganisations,
                    AddressFetcherImpl::new,
                    Organisation::getId,
                    organisation -> mapNullable(organisation.getFactualAddress(), BaseModel::getId),
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

    private static class EmployeeFetcherImpl extends BaseModelFetcherImpl<Employee> implements EmployeeFetcher {

        private EmployeeFetcherImpl(Employee src) {
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

    private static class ContactFetcherImpl extends BaseModelFetcherImpl<Contact> implements ContactFetcher {

        public ContactFetcherImpl(Contact contact) {
            super(contact);
        }

        @Override
        public void fetchPhone(Contact model) {
            model.setPhone(src.getPhone());
        }

        @Override
        public void fetchEmail(Contact model) {
            model.setEmail(src.getEmail());
        }

        @Override
        public Contact create() {
            return new Contact();
        }

        @Override
        public void fetchId(Contact model) {
            model.setId(src.getId());
        }

    }

    private static class ProjectFetcherImpl extends BaseModelFetcherImpl<Project> implements ProjectFetcher {

        public ProjectFetcherImpl(Project project) {
            super(project);
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
            model.setName(src.getName());
        }

        @Override
        public Project create() {
            return new Project();
        }

    }

    private static class OrganisationFetcherImpl extends BaseModelFetcherImpl<Organisation> implements OrganisationFetcher {

        private OrganisationFetcherImpl(Organisation src) {
            super(src);
        }

        @Override
        public Organisation create() {
            return new Organisation();
        }

        @Override
        public void setLegalAddress(Organisation model, Address legalAddress) {
            model.setLegalAddress(legalAddress);
        }

        @Override
        public AddressFetcher legalAddressFetcher() {
            return mapNullable(src.getLegalAddress(), AddressFetcherImpl::new);
        }

        @Override
        public void setFactualAddress(Organisation model, Address factualAddress) {
            model.setFactualAddress(factualAddress);
        }

        @Override
        public AddressFetcher factualAddressFetcher() {
            return mapNullable(src.getFactualAddress(), AddressFetcherImpl::new);
        }

        @Override
        public void fetchName(Organisation model) {
            model.setName(src.getName());
        }

    }

    private static class AddressFetcherImpl extends BaseModelFetcherImpl<Address> implements AddressFetcher {

        private AddressFetcherImpl(Address src) {
            super(src);
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

    }

    private static class PassportFetcherImpl extends BaseModelFetcherImpl<Passport> implements PassportFetcher {

        private PassportFetcherImpl(Passport src) {
            super(src);
        }

        @Override
        public Passport create() {
            return new Passport();
        }

        @Override
        public void fetchId(Passport model) {
            model.setId(src.getId());
        }

        @Override
        public void fetchSeries(Passport model) {
            model.setSeries(src.getSeries());
        }

        @Override
        public void fetchNumber(Passport model) {
            model.setNumber(src.getNumber());
        }

    }

    private static abstract class BaseModelFetcherImpl<T extends BaseModel> implements BaseModelFetcher<T> {

        protected final T src;

        protected BaseModelFetcherImpl(T src) {
            this.src = src;
        }

        @Override
        public void fetchId(T model) {
            model.setId(src.getId());
        }

    }

    private static <E1, E2> E2 mapNullable(E1 e1, Function<? super E1, ? extends E2> mapper) {
        return e1 == null ? null : mapper.apply(e1);
    }

}
