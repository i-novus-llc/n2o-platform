package net.n2oapp.platform.test.autoconfigure;

import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.PrettyLoggingFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;


/**
 * @author lgalimova
 * @since 02.04.2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        properties = {
                "jaxrs.logging-in.enabled=true",
                "jaxrs.logging-in.limit=1024",
                "jaxrs.logging-in.in-mem-threshold=102400",
                "jaxrs.logging-in.log-binary=true",
                "jaxrs.logging-in.log-multipart=true",
                "jaxrs.logging-in.pretty-logging=true",
        },
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DefinePort
public class DefineLoggingAttributesTest {

    @Autowired
    private LoggingInInterceptor loggingInInterceptor;

    @Test
    public void testLimit() {
        assertEquals(1024, loggingInInterceptor.getLimit());
    }

    @Test
    public void testInMemThreshold() {
        assertEquals(100 * 1024, loggingInInterceptor.getInMemThreshold());
    }

    @Test
    public void testLogBinary() throws NoSuchFieldException, IllegalAccessException {
        Field privateLogBinaryField = AbstractLoggingInterceptor.class.getDeclaredField("logBinary");
        privateLogBinaryField.setAccessible(true);
        assertEquals(true, privateLogBinaryField.get(loggingInInterceptor));
    }

    @Test
    public void testLogMultipart() throws NoSuchFieldException, IllegalAccessException {
        Field privateLogMultipartField = AbstractLoggingInterceptor.class.getDeclaredField("logMultipart");
        privateLogMultipartField.setAccessible(true);
        assertEquals(true, privateLogMultipartField.get(loggingInInterceptor));
    }

    @Test
    public void testPrettyLogging() throws NoSuchFieldException, IllegalAccessException {
        Field privateSenderField = AbstractLoggingInterceptor.class.getDeclaredField("sender");
        privateSenderField.setAccessible(true);
        LogEventSender sender = (LogEventSender) privateSenderField.get(loggingInInterceptor);

        if (sender instanceof PrettyLoggingFilter) {
            Field privatePrettyField = PrettyLoggingFilter.class.getDeclaredField("prettyLogging");
            privatePrettyField.setAccessible(true);
            assertEquals(true, privatePrettyField.get(sender));
        }
    }
}

