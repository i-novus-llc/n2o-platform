package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.jaxrs.api.SomeRest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.TestSocketUtils;
import java.net.http.HttpTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
class DefineReceiveTimeoutTest {

    @BeforeAll
    public static void init() {
        System.setProperty("server.port", String.valueOf(TestSocketUtils.findAvailableTcpPort()));
    }

    @AfterAll
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
    void testReceiveTimeoutSuccess() throws InterruptedException {
        assertEquals("timeout success", client.timeoutSuccess());
    }

    /*
     * При выполнении запроса дольше указанного таймаута ожидается ошибка
     * (превышен таймаут ожидания ответа).
     * Погрешность равна 1 секунде
     * */
    @Test
    void testReceiveTimeoutFail() {
        long start = System.currentTimeMillis();
        try {
            client.timeoutFailure();
            fail("receive timeout exception is expected");
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            assertTrue(e.getCause() instanceof HttpTimeoutException);
            assertTrue(end - start >= 1000, "timeout must be approximately 1 sec");
            assertTrue(end - start < 2000, "margin of error less than 1 sec");
        }
    }

}