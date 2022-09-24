package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.jaxrs.api.SomeRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class,
        properties = {
                "cxf.servlet.init.service-list-path=/info",
                "cxf.path=/test/api",
                "cxf.jaxrs.component-scan=true",
                "cxf.jaxrs.client.classes-scan=true",
                "cxf.jaxrs.client.classes-scan-packages=net.n2oapp.platform.jaxrs.api",
                "cxf.jaxrs.client.address=http://10.10.10.10:1010/test/api",
                "cxf.jaxrs.client.connection.timeout=1000",
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefineConnectionTimeoutTest {

    @Autowired
    @Qualifier("someRestJaxRsProxyClient")
    private SomeRest client;

    /*
    * При обращении к серверу по невалидному адресу по истечении указанного таймаута ожидается ошибка
    * (превышен таймаут ожидания соединения).
    * Погрешность равна 1 секунде
    * */
    @Test
    public void testConnectionTimeoutFail() {
        long start = System.currentTimeMillis();
        try {
            client.timeoutSuccess();
            fail("connection timeout exception is expected");
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            assertTrue(e.getCause() instanceof SocketTimeoutException);
            assertTrue(end - start >= 1000, "timeout must be approximately 1 sec");
            assertTrue(end - start < 2000, "margin of error less than 1 sec");
        }
    }

}