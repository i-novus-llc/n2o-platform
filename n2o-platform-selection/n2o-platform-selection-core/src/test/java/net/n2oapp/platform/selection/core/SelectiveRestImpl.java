package net.n2oapp.platform.selection.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class SelectiveRestImpl implements SelectiveRest {

    private static final List<Employee> EMPLOYEES;
    static {
        EMPLOYEES = new ArrayList<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < 100; i++) {
            Employee emp = new Employee();
            emp.id = rnd.nextInt();
            byte[] temp = new byte[10];
            rnd.nextBytes(temp);
            emp.name = new String(temp);
            emp.organisation = new Organisation();
            emp.organisation.id = rnd.nextInt();
            emp.organisation.legalAddress = new Address();
            emp.organisation.factualAddress = new Address();
            rnd.nextBytes(temp);
            emp.organisation.legalAddress.postcode = new String(temp);
            rnd.nextBytes(temp);
            emp.organisation.legalAddress.region = new String(temp);
            rnd.nextBytes(temp);
            emp.organisation.factualAddress.postcode = new String(temp);
            rnd.nextBytes(temp);
            emp.organisation.factualAddress.region = new String(temp);
            emp.contacts = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                Employee.Contact contact = new Employee.Contact();
                rnd.nextBytes(temp);
                contact.email = new String(temp);
                rnd.nextBytes(temp);
                contact.phone = new String(temp);
                emp.contacts.add(contact);
            }
            EMPLOYEES.add(emp);
        }
    }

    @Override
    public Page<Employee> search(EmployeeCriteria criteria) {
        return Selector.resolvePage(
            new PageImpl<>(EMPLOYEES).map(EmployeeFetcherImpl::new),
            criteria.selection()
        );
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
        public void selectId(Employee model) {
            model.setId(src.getId());
        }

        @Override
        public void selectName(Employee model) {
            model.setName(src.getName());
        }

        @Override
        public void selectOrganisation(Employee model, Organisation organisation) {
            model.setOrganisation(organisation);
        }

        @Override
        public OrganisationFetcher organisationFetcher() {
            return new OrganisationFetcherImpl(src.getOrganisation());
        }

        @Override
        public void selectContacts(Employee model, List<Employee.Contact> contacts) {
            model.setContacts(contacts);
        }

        @Override
        public List<? extends ContactFetcher> contactsFetcher() {
            return src.contacts == null ? Collections.emptyList() : src.contacts.stream().map(contact -> new ContactFetcher() {
                @Override
                public void selectPhone(Employee.Contact model) {
                    model.setPhone(contact.getPhone());
                }

                @Override
                public void selectEmail(Employee.Contact model) {
                    model.setEmail(contact.getEmail());
                }

                @Override
                public Employee.Contact create() {
                    return new Employee.Contact();
                }
            }).collect(Collectors.toList());
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
        public void selectId(Organisation model) {
            model.setId(src.getId());
        }

        @Override
        public void selectLegalAddress(Organisation model, Address legalAddress) {
            model.setLegalAddress(legalAddress);
        }

        @Override
        public AddressFetcher legalAddressFetcher() {
            return new AddressFetcherImpl(src.getLegalAddress());
        }

        @Override
        public void selectFactualAddress(Organisation model, Address factualAddress) {
            model.setFactualAddress(factualAddress);
        }

        @Override
        public AddressFetcher factualAddressFetcher() {
            return new AddressFetcherImpl(src.getFactualAddress());
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
        public void selectPostcode(Address model) {
            model.setPostcode(src.getPostcode());
        }

        @Override
        public void selectRegion(Address model) {
            model.setRegion(src.getRegion());
        }

        @Override
        public void selectId(Address model) {
            model.setId(src.getId());
        }

    }

}
