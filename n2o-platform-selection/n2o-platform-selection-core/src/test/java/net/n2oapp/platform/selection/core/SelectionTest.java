package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.jaxrs.autoconfigure.EnableJaxRsProxyClient;
import net.n2oapp.platform.selection.api.SelectionPropagationEnum;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.SingleQueryCountHolder;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = SelectionTest.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@EnableJaxRsProxyClient(value = SelectiveRest.class, address = "http://localhost:8425/api")
@Import(SelectionTest.Config.class)
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
        byte[] bytes = new byte[10];
        ThreadLocalRandom.current().nextBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static boolean randBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    @Before
    public void setup() {
        int numProjects = 10;
        List<Project> projects = new ArrayList<>(numProjects);
        for (int i = 0; i < numProjects; i++) {
            Project project = new Project();
            project.setName(randString());
            projects.add(projectRepository.save(project));
        }
        int numAddresses = 20;
        List<Integer> addresses = new ArrayList<>(numAddresses);
        for (int i = 0; i < numAddresses; i++) {
            Address address = new Address();
            address.setPostcode(randString());
            address.setRegion(randString());
            addresses.add(addressRepository.save(address).getId());
        }
        int numOrgs = 5;
        List<Integer> orgs = new ArrayList<>(numOrgs);
        for (int i = 0; i < numOrgs; i++) {
            Organisation org = new Organisation();
            org.setName(randString());
            if (randBoolean())
                org.setLegalAddress(new Address(addresses.get(ThreadLocalRandom.current().nextInt(numAddresses))));
            if (randBoolean())
                org.setFactualAddress(new Address(addresses.get(ThreadLocalRandom.current().nextInt(numAddresses))));
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
            employeeRepository.save(employee);
        }
    }

    @Test
    public void test() {
        EmployeeCriteria criteria = new EmployeeCriteria();
        criteria.setPageSize(100);
        DefaultEmployeeSelection selection = EmployeeSelection.create().id().name().contacts(
                ContactSelection.create().phone()
        ).organisation(
                OrganisationSelection.create().name().factualAddress(
                        AddressSelection.create().postcode()
                ).legalAddress(
                        AddressSelection.create().region()
                )
        ).projects(ProjectSelection.create().name());
        criteria.setSelection(selection);
        queryCount.setSelect(0);
        Page<Employee> page = client.search(criteria);
        assertEquals(7, queryCount.getSelect());
        check(page);
        criteria.setSelection(selection.unselectContacts());
        queryCount.setSelect(0);
        page = client.search(criteria);
        assertEquals(6, queryCount.getSelect());
        for (Employee employee : page)
            assertNull(employee.getContacts());
        criteria.setSelection(selection.propagate(SelectionPropagationEnum.NESTED));
        queryCount.setSelect(0);
        client.search(criteria);
        assertEquals(7, queryCount.getSelect());
        criteria.setSelection(selection.propagate(SelectionPropagationEnum.ALL).unselectProjects().unselectOrganisation());
        queryCount.setSelect(0);
        client.search(criteria);
        assertEquals(2, queryCount.getSelect());
    }

    private void check(Page<Employee> page) {
        TransactionTemplate template = new TransactionTemplate(txManager);
        template.execute(status -> {
            for (Employee actual : page.getContent()) {
                assertNotNull(actual.getId());
                Employee expected = employeeRepository.findById(actual.getId()).get();
                assertNotNull(actual.getName());
                assertEquals(expected.getName(), actual.getName());
                if (actual.getContacts() != null) {
                    assertNotNull(expected.getContacts());
                    assertEquals(expected.getContacts().size(), actual.getContacts().size());
                    Iterator<Contact> iterator = expected.getContacts().iterator();
                    for (Contact actualContact : actual.getContacts()) {
                        assertNull(actualContact.getEmail());
                        assertNotNull(actualContact.getPhone());
                        Contact expectedContact = iterator.next();
                        assertEquals(expectedContact.getPhone(), actualContact.getPhone());
                    }
                } else {
                    assertEquals(emptyList(), expected.getContacts());
                }
                if (actual.getOrganisation() != null) {
                    assertNotNull(expected.getOrganisation());
                    assertNull(actual.getOrganisation().getId());
                    assertNotNull(actual.getOrganisation().getName());
                    assertEquals(expected.getOrganisation().getName(), actual.getOrganisation().getName());
                    if (actual.getOrganisation().getLegalAddress() != null) {
                        assertNotNull(expected.getOrganisation().getLegalAddress());
                        assertNull(actual.getOrganisation().getLegalAddress().getId());
                        assertNull(actual.getOrganisation().getLegalAddress().getPostcode());
                        assertNotNull(actual.getOrganisation().getLegalAddress().getRegion());
                        assertEquals(expected.getOrganisation().getLegalAddress().getRegion(), actual.getOrganisation().getLegalAddress().getRegion());
                    }
                    if (actual.getOrganisation().getFactualAddress() != null) {
                        assertNotNull(expected.getOrganisation().getFactualAddress());
                        assertNull(actual.getOrganisation().getFactualAddress().getId());
                        assertNull(actual.getOrganisation().getFactualAddress().getRegion());
                        assertNotNull(actual.getOrganisation().getFactualAddress().getPostcode());
                        assertEquals(expected.getOrganisation().getFactualAddress().getPostcode(), actual.getOrganisation().getFactualAddress().getPostcode());
                    }
                } else
                    assertNull(expected.getOrganisation());
                if (!CollectionUtils.isEmpty(actual.getProjects())) {
                    assertFalse(expected.getProjects().isEmpty());
                    for (Project project : expected.getProjects()) {
                        Optional<Project> opt = actual.getProjects().stream().filter(p -> p.getName().equals(project.getName())).findAny();
                        assertTrue(opt.isPresent());
                    }
                } else
                    assertTrue(expected.getProjects().isEmpty());
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
