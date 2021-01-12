package net.n2oapp.platform.ms.autoconfigure;

import ch.qos.logback.core.CoreConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OneLineLoggingTest.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"logging.config=classpath:logback-oneline.xml"})
@EnableAutoConfiguration
public class OneLineLoggingTest {

    private static final Logger logger = LoggerFactory.getLogger("ROOT");
    private static final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private static final String messageWithLineSeparator = "\n Test message \n with \r some \r\n line \n\r separator \r";

    @BeforeAll
    public static void configure() {
        System.setOut(new PrintStream(outputStream));
    }

    static PrintStream output = System.out;

    @Test
    public void testOnelineLogging() {
        outputStream.reset();
        logger.info(messageWithLineSeparator);
        String loggedMessage = outputStream.toString();
        assertTrue(loggedMessage.endsWith(CoreConstants.LINE_SEPARATOR));
        loggedMessage = loggedMessage.substring(0, loggedMessage.length() - CoreConstants.LINE_SEPARATOR_LEN);
        assertFalse(loggedMessage.contains(CoreConstants.LINE_SEPARATOR));
        assertFalse(loggedMessage.contains("\n"));
        assertFalse(loggedMessage.contains("\r"));
        assertFalse(loggedMessage.contains("\r\n"));

        outputStream.reset();
        logger.error(messageWithLineSeparator, exceptionWithStackTrace());
        String loggedException = outputStream.toString();
        assertTrue(loggedException.endsWith(CoreConstants.LINE_SEPARATOR));
        loggedException = loggedException.substring(0, loggedException.length() - CoreConstants.LINE_SEPARATOR_LEN);
        assertFalse(loggedException.contains(CoreConstants.LINE_SEPARATOR));
        assertFalse(loggedException.contains("\n"));
        assertFalse(loggedException.contains("\r"));
        assertFalse(loggedException.contains("\r\n"));
    }

    private RuntimeException exceptionWithStackTrace() {
        RuntimeException runtimeException = new RuntimeException(messageWithLineSeparator);
        runtimeException.setStackTrace(Thread.currentThread().getStackTrace());
        runtimeException.initCause(new NullPointerException());
        runtimeException.addSuppressed(new ArrayIndexOutOfBoundsException(messageWithLineSeparator));
        return runtimeException;
    }
}