package net.n2oapp.platform.selection.core;

import net.n2oapp.platform.jaxrs.autoconfigure.EnableJaxRsProxyClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootApplication
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = SelectionTest.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@EnableJaxRsProxyClient(value = SelectiveRest.class, address = "http://localhost:8425/api")
public class SelectionTest {

    @Autowired
    @Qualifier("selectiveRestJaxRsProxyClient")
    private SelectiveRest client;

    @Test
    public void name() {
        EmployeeCriteria criteria = new EmployeeCriteria();
        criteria.setSelection(
            (DefaultEmployeeSelection) EmployeeSelection.create().contacts(
                ContactSelection.create().phone()
            ).name().organisation(
                OrganisationSelection.create().factualAddress(
                    AddressSelection.create().postcode()
                ).legalAddress(
                    AddressSelection.create().region()
                )
            ).id()
        );
        Page<Employee> page = client.search(criteria);
        for (Employee employee : page) {
            assertNotNull(employee.getId());
            assertNotNull(employee.getName());
            assertNotNull(employee.getContacts());
            for (Employee.Contact contact : employee.getContacts()) {
                assertNotNull(contact.getPhone());
                assertNull(contact.getEmail());
            }
            assertNotNull(employee.getOrganisation());
            assertNull(employee.getOrganisation().getId());
            assertNotNull(employee.getOrganisation().getLegalAddress());
            assertNull(employee.getOrganisation().getLegalAddress().getId());
            assertNotNull(employee.getOrganisation().getFactualAddress());
            assertNull(employee.getOrganisation().getFactualAddress().getId());
            assertNull(employee.getOrganisation().getFactualAddress().getRegion());
            assertNotNull(employee.getOrganisation().getFactualAddress().getPostcode());
            assertNull(employee.getOrganisation().getLegalAddress().getPostcode());
            assertNotNull(employee.getOrganisation().getLegalAddress().getRegion());
        }
    }

}
