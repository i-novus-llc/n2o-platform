package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.jaxrs.api.SomeRest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import java.net.SocketTimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class,
        properties = {
                "cxf.servlet.init.service-list-path=/info",
                "cxf.path=/test/api",
                "cxf.jaxrs.component-scan=true",
                "cxf.jaxrs.client.classes-scan=true",
                "cxf.jaxrs.client.classes-scan-packages=net.n2oapp.platform.jaxrs.api",
                "cxf.jaxrs.client.address=http://localhost:${server.port}/test/api",
                "cxf.jaxrs.client.receive.timeout=1000",
        },
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DefineReceiveTimeoutTest {

    @BeforeClass
    public static void init() {
        System.setProperty("server.port", String.valueOf(SocketUtils.findAvailableTcpPort()));
    }

    @AfterClass
    public static void destroy() {
        System.clearProperty("server.port");
    }

    @Autowired
    @Qualifier("someRestJaxRsProxyClient")
    private SomeRest client;

    /*
     * Проверка, что нет ошибки при выполнении запроса быстрее, чем указано в таймауте
     * */
    @Test
    public void testReceiveTimeoutSuccess() throws InterruptedException {
        assertEquals("timeout success", client.timeoutSuccess());
    }

    /*
     * При выполнении запроса дольше указанного таймаута ожидается ошибка
     * (превышен таймаут ожидания ответа).
     * Погрешность равна 1 секунде
     * */
    @Test
    public void testReceiveTimeoutFail() {
        long start = System.currentTimeMillis();
        try {
            client.timeoutFailure();
            fail("receive timeout exception is expected");
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            assertTrue(e.getCause() instanceof SocketTimeoutException);
            assertTrue("timeout must be approximately 1 sec", end - start >= 1000);
            assertTrue("margin of error less than 1 sec", end - start < 2000);
        }
    }

}