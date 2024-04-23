package net.n2oapp.platform.ms.autoconfigure.app;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.ms.autoconfigure.MemoryAppender;
import net.n2oapp.platform.ms.autoconfigure.logging.CustomLogstashLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = JsonFormatTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "n2o.ms.logging.json.enabled=true"})
@EnableAutoConfiguration
public class JsonFormatTest {

    private static final String TEST_LOG_MESSAGE = "test";
    private MemoryAppender memoryAppender;

    @BeforeEach
    void init() {
        Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    void testJsonFormat() {
        //Layout and encoder substitution test
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Layout<ILoggingEvent> layout = null;
        for (Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();
                if (!(appender instanceof MemoryAppender)) {
                    assertThat(appender, instanceOf(OutputStreamAppender.class));
                    Encoder<ILoggingEvent> encoder = ((OutputStreamAppender<ILoggingEvent>) appender).getEncoder();
                    assertThat(encoder, instanceOf(LayoutWrappingEncoder.class));
                    layout = ((LayoutWrappingEncoder<ILoggingEvent>) encoder).getLayout();
                    assertThat(layout, instanceOf(CustomLogstashLayout.class));
                }
            }
        }

        // Check Json Format
        Logger log = (Logger) LoggerFactory.getLogger(JsonFormatTest.class);
        log.info(TEST_LOG_MESSAGE);
        ILoggingEvent event = memoryAppender.findFirst(TEST_LOG_MESSAGE);
        assertThat(isJsonValid(layout.doLayout(event)), is(true));
    }

    private boolean isJsonValid(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}