package net.n2oapp.platform.test.autoconfigure;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @author lgalimova
 * @since 02.04.2019
 */
@ActiveProfiles("Logging-test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        properties = {
                "cxf.servlet.init.service-list-path=/info",
                "cxf.path=/test/api",
                "cxf.jaxrs.component-scan=true",
                "cxf.jaxrs.client.classes-scan=true",
                "cxf.jaxrs.client.classes-scan-packages=net.n2oapp.platform.test.autoconfigure.rest.api",
                "cxf.jaxrs.client.address=http://10.10.10.10:1010/test/api",
                "jaxrs.logging-in.enabled=true",
                "jaxrs.logging-in.limit=1024",
                "jaxrs.logging-in.in-mem-threshold=102400",
        },
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DefineLoggingAttributesTest {

        @Autowired
        @SpyBean
        private LoggingInInterceptor loggingInInterceptor;

        @Test
        public void testLoggingAttributes(){
                Assert.assertEquals(1024, loggingInInterceptor.getLimit());
                Assert.assertEquals(100*1024, loggingInInterceptor.getInMemThreshold());
        }
}
