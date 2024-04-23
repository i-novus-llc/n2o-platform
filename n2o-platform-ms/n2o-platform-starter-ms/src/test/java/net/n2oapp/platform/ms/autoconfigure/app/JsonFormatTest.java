package net.n2oapp.platform.ms.autoconfigure.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import net.n2oapp.platform.ms.autoconfigure.logging.CustomLogstashLayout;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

@SpringBootTest(classes = JsonFormatTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "n2o.ms.logging.json.enabled=true"})
@EnableAutoConfiguration
public class JsonFormatTest {

    @Test
    void testCustomLayout() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();
                assertThat(appender, instanceOf(OutputStreamAppender.class));
                Encoder<ILoggingEvent> encoder = ((OutputStreamAppender<ILoggingEvent>) appender).getEncoder();
                assertThat(encoder, instanceOf(LayoutWrappingEncoder.class));
                Layout<ILoggingEvent> layout = ((LayoutWrappingEncoder<ILoggingEvent>) encoder).getLayout();
                assertThat(layout, instanceOf(CustomLogstashLayout.class));
            }
        }
    }
}