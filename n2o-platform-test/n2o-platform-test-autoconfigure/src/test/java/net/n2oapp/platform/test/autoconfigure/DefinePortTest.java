package net.n2oapp.platform.test.autoconfigure;

import net.n2oapp.platform.test.autoconfigure.rest.api.SomeRest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        properties = {
                "cxf.servlet.init.service-list-path=/info",
                "cxf.path=/test/api",
                "cxf.jaxrs.component-scan=true",
                "cxf.jaxrs.client.classes-scan=true",
                "cxf.jaxrs.client.classes-scan-packages=net.n2oapp.platform.test.autoconfigure.rest.api",
                "cxf.jaxrs.client.address=http://localhost:${server.port}/test/api",
        },
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DefinePort
public class DefinePortTest {

    @Autowired
    @Qualifier("someRestJaxRsProxyClient")
    private SomeRest client;

    @Test
    public void testClientCall() throws Exception {
        Assert.assertEquals("echo", client.echo());
    }
}
