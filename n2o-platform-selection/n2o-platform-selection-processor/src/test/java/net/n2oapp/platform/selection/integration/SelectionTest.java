package net.n2oapp.platform.selection.integration;

import net.n2oapp.platform.jaxrs.autoconfigure.EnableJaxRsProxyClient;
import net.n2oapp.platform.selection.api.SelectionPropagation;
import net.n2oapp.platform.selection.integration.model.*;
import net.n2oapp.platform.selection.integration.repository.AddressRepository;
import net.n2oapp.platform.selection.integration.repository.EmployeeRepository;
import net.n2oapp.platform.selection.integration.repository.OrganisationRepository;
import net.n2oapp.platform.selection.integration.repository.ProjectRepository;
import net.n2oapp.platform.selection.integration.rest.EmployeeCriteria;
import net.n2oapp.platform.selection.integration.rest.SelectiveRest;
import net.n2oapp.platform.test.autoconfigure.DefinePort;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.SingleQueryCountHolder;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.emptyList;

@SpringBootApplication
@SpringBootTest(
    classes = SelectionTest.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
        "cxf.path=/api",
        "cxf.jaxrs.component-scan=true",
        "spring.jpa.properties.hibernate.show_sql=false",
        "spring.jpa.properties.javax.persistence.validation.mode=none",
        "spring.jpa.properties.hibernate.check_nullability=false"
    }
)
@EnableJaxRsProxyClient(value = SelectiveRest.class, address = "http://localhost:${server.port}/api")
@Import(SelectionTest.Config.class)
@DefinePort
public class SelectionTest {

    private static QueryCount queryCount;

    @Autowired
    @Qualifier("selectiveRestJaxRsProxyClient")
    private SelectiveRest client;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PlatformTransactionManager txManager;

    private static String randString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            builder.append((char) ThreadLocalRandom.current().nextInt('a', 'z' + 1));
        }
        return builder.toString();
    }

    private static boolean randBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    @BeforeEach
    public void setup() {
        int numProjects = 10;
        List<Project> projects = new ArrayList<>(numProjects);
        for (int i = 0; i < numProjects; i++) {
            Project project = new Project();
            project.setName(randString());
            projects.add(projectRepository.save(project));
        }
        int numAddresses = 20;
        List<Integer> legalAddresses = new ArrayList<>(numAddresses / 2);
        List<Integer> factualAddresses = new ArrayList<>(numAddresses / 2);
        for (int i = 0; i < numAddresses; i++) {
            Address address = new Address();
            address.setPostcode(randString());
            address.setRegion(randString());
            final Integer id = addressRepository.save(address).getId();
            if (randBoolean()) {
                legalAddresses.add(id);
            } else {
                factualAddresses.add(id);
            }
        }
        int numOrgs = 5;
        List<Integer> orgs = new ArrayList<>(numOrgs);
        for (int i = 0; i < numOrgs; i++) {
            Organisation org = new Organisation();
            org.setName(randString());
            if (randBoolean())
                org.setLegalAddress(new Address(legalAddresses.get(ThreadLocalRandom.current().nextInt(legalAddresses.size()))));
            if (randBoolean())
                org.setFactualAddress(new Address(factualAddresses.get(ThreadLocalRandom.current().nextInt(factualAddresses.size()))));
            orgs.add(organisationRepository.save(org).getId());
        }
        for (int i = 0; i < 100; i++) {
            Employee employee = new Employee();
            employee.setName(randString());
            if (randBoolean()) {
                employee.setOrganisation(new Organisation(orgs.get(ThreadLocalRandom.current().nextInt(numOrgs))));
            }
            if (randBoolean()) {
                int numContacts = 3;
                List<Contact> contacts = new ArrayList<>(numContacts);
                for (int j = 0; j < numContacts; j++) {
                    Contact contact = new Contact();
                    contact.setEmail(randString());
                    contact.setPhone(randString());
                    contact.setOwner(employee);
                    contacts.add(contact);
                }
                employee.setContacts(contacts);
            }
            if (randBoolean()) {
                int numProjectsWorkingOn = 2;
                for (int j = 0; j < numProjectsWorkingOn; j++) {
                    Project project = projects.get(ThreadLocalRandom.current().nextInt(projects.size()));
                    employee.getProjects().add(project);
                    project.getWorkers().add(employee);
                }
            }
            Passport passport = new Passport();
            passport.setNumber(randString());
            passport.setSeries(randString());
            employee.setPassport(passport);
            employeeRepository.save(employee);
        }
    }

    @Test
    public void test() {
        EmployeeCriteria criteria = new EmployeeCriteria();
        criteria.setPageSize(100);
        EmployeeSelection selection = EmployeeSelection.create().id().name().contacts(
                ContactSelection.create().phone()
        ).organisation(
                OrganisationSelection.create().name().factualAddress(
                        AddressSelection.create().postcode()
                ).legalAddress(
                        AddressSelection.create().region()
                )
        ).projects(
            ProjectSelection.create().name()
        ).passport(
            PassportSelection.create().series()
        );
        criteria.setSelection(selection);
        queryCount.setSelect(0);
        Page<Employee> page = client.search(criteria);
        Assertions.assertEquals(8, queryCount.getSelect());
        check(page);
        criteria.setSelection(selection.unselectContacts());
        queryCount.setSelect(0);
        page = client.search(criteria);
        Assertions.assertEquals(7, queryCount.getSelect());
        for (Employee employee : page)
            Assertions.assertNull(employee.getContacts());
        criteria.setSelection(selection.propagate(SelectionPropagation.NESTED));
        queryCount.setSelect(0);
        client.search(criteria);
        Assertions.assertEquals(8, queryCount.getSelect());
        criteria.setSelection(selection.propagate(SelectionPropagation.ALL).unselectProjects().unselectOrganisation().unselectPassport());
        queryCount.setSelect(0);
        client.search(criteria);
        Assertions.assertEquals(2, queryCount.getSelect());
    }

    private void check(Page<Employee> page) {
        TransactionTemplate template = new TransactionTemplate(txManager);
        template.execute(status -> {
            for (Employee actual : page.getContent()) {
                Assertions.assertNotNull(actual.getId());
                Employee expected = employeeRepository.findById(actual.getId()).get();
                Assertions.assertNotNull(actual.getName());
                Assertions.assertEquals(expected.getName(), actual.getName());
                if (actual.getContacts() != null) {
                    Assertions.assertNotNull(expected.getContacts());
                    Assertions.assertEquals(expected.getContacts().size(), actual.getContacts().size());
                    Iterator<Contact> iterator = expected.getContacts().iterator();
                    for (Contact actualContact : actual.getContacts()) {
                        Assertions.assertNull(actualContact.getEmail());
                        Assertions.assertNotNull(actualContact.getPhone());
                        Contact expectedContact = iterator.next();
                        Assertions.assertEquals(expectedContact.getPhone(), actualContact.getPhone());
                    }
                } else {
                    Assertions.assertEquals(emptyList(), expected.getContacts());
                }
                if (actual.getOrganisation() != null) {
                    Assertions.assertNotNull(expected.getOrganisation());
                    Assertions.assertNull(actual.getOrganisation().getId());
                    Assertions.assertNotNull(actual.getOrganisation().getName());
                    Assertions.assertEquals(expected.getOrganisation().getName(), actual.getOrganisation().getName());
                    if (actual.getOrganisation().getLegalAddress() != null) {
                        Assertions.assertNotNull(expected.getOrganisation().getLegalAddress());
                        Assertions.assertNull(actual.getOrganisation().getLegalAddress().getId());
                        Assertions.assertNull(actual.getOrganisation().getLegalAddress().getPostcode());
                        Assertions.assertNotNull(actual.getOrganisation().getLegalAddress().getRegion());
                        Assertions.assertEquals(expected.getOrganisation().getLegalAddress().getRegion(), actual.getOrganisation().getLegalAddress().getRegion());
                    }
                    if (actual.getOrganisation().getFactualAddress() != null) {
                        Assertions.assertNotNull(expected.getOrganisation().getFactualAddress());
                        Assertions.assertNull(actual.getOrganisation().getFactualAddress().getId());
                        Assertions.assertNull(actual.getOrganisation().getFactualAddress().getRegion());
                        Assertions.assertNotNull(actual.getOrganisation().getFactualAddress().getPostcode());
                        Assertions.assertEquals(expected.getOrganisation().getFactualAddress().getPostcode(), actual.getOrganisation().getFactualAddress().getPostcode());
                    }
                } else
                    Assertions.assertNull(expected.getOrganisation());
                if (!CollectionUtils.isEmpty(actual.getProjects())) {
                    Assertions.assertFalse(expected.getProjects().isEmpty());
                    for (Project project : expected.getProjects()) {
                        Optional<Project> opt = actual.getProjects().stream().filter(p -> p.getName().equals(project.getName())).findAny();
                        Assertions.assertTrue(opt.isPresent());
                        Project actualProject = opt.get();
                        Assertions.assertNotNull(actualProject.getName());
                        Assertions.assertNull(actualProject.getId());
                    }
                } else
                    Assertions.assertTrue(expected.getProjects().isEmpty());
                Assertions.assertNotNull(actual.getPassport());
                Assertions.assertNull(actual.getPassport().getNumber());
                Assertions.assertNull(actual.getPassport().getId());
                Assertions.assertNotNull(actual.getPassport().getSeries());
                Assertions.assertEquals(expected.getPassport().getSeries(), actual.getPassport().getSeries());
            }
           return null;
        });
    }

    @Configuration
    public static class Config {
        @Bean
        public DataSourceBeanPostProcessor dataSourceBeanPostProcessor() {
            return new DataSourceBeanPostProcessor();
        }
    }

    public static class DataSourceBeanPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
            if (bean instanceof DataSource) {
                DataSource dataSourceBean = (DataSource) bean;
                ChainListener listener = new ChainListener();
                SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
                listener.addListener(loggingListener);
                DataSourceQueryCountListener queryCountListener = new DataSourceQueryCountListener();
                queryCountListener.setQueryCountStrategy(new SingleQueryCountHolder());
                listener.addListener(queryCountListener);
                SelectionTest.queryCount = queryCountListener.getQueryCountStrategy().getOrCreateQueryCount("DataSourceCounter");
                return ProxyDataSourceBuilder
                        .create(dataSourceBean)
                        .name("DataSourceCounter")
                        .listener(listener)
                        .build();
            }
            return bean;
        }
    }

}
