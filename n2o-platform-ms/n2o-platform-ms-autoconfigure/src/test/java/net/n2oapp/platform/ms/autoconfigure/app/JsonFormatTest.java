package net.n2oapp.platform.ms.autoconfigure.app;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import net.logstash.logback.composite.JsonProviders;
import net.n2oapp.platform.ms.autoconfigure.MemoryAppender;
import net.n2oapp.platform.ms.autoconfigure.logging.CustomLogstashLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = JsonFormatTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"n2o.ms.logging.json.enabled=true",
                "n2o.ms.logging.json.provider.include_names=net.n2oapp.platform.ms.autoconfigure.CustomLogstashLayoutProvider",
                "n2o.ms.logging.json.timestamp.field_name=timestamp_test"})
@EnableAutoConfiguration
public class JsonFormatTest {

    private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(JsonFormatTest.class);
    private final static Instant NOW = LocalDateTime.of(2025, 6, 18, 16, 43).toInstant(getSystemDefaultOffset());
    private static final String TEST_LOG_MESSAGE = "test";
    private final static String EXPECTED_JSON_MESSAGE = "{\"timestamp_test\":\"2025-06-18 16:43:00.000\",\"message\":[\"test\"],\"logger_name\":\"net.n2oapp.platform.ms.autoconfigure.app.JsonFormatTest\",\"thread_name\":\"main\",\"level\":\"INFO\",\"@version\":\"1\"}" + System.lineSeparator();
    private static final String CUSTOM_PROVIDER_NAME = "net.n2oapp.platform.ms.autoconfigure.CustomLogstashLayoutProvider";
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
        //Layout and encoder config test
        Layout<ILoggingEvent> layout = asserConfigAndGetLayout();

        // Check Json Format
        LOGGER.info(TEST_LOG_MESSAGE);

        ILoggingEvent event = memoryAppender.findFirst(TEST_LOG_MESSAGE);
        event = mockTimeStamp(event);


        String jsonLog = layout.doLayout(event);
        assertThat(EXPECTED_JSON_MESSAGE, equalTo(jsonLog));
    }

    private ILoggingEvent mockTimeStamp(ILoggingEvent event) {
        ILoggingEvent spyEvent = Mockito.spy(event);
        Mockito.doReturn(NOW).when(spyEvent).getInstant();
        Mockito.doReturn(NOW.toEpochMilli()).when(spyEvent).getTimeStamp();
        return spyEvent;
    }

    private CustomLogstashLayout asserConfigAndGetLayout() {
        Logger root = ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger(Logger.ROOT_LOGGER_NAME);
        OutputStreamAppender<ILoggingEvent> outputAppender = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(root.iteratorForAppenders(), Spliterator.ORDERED),
                        false
                ).filter(iLoggingEventAppender -> iLoggingEventAppender instanceof OutputStreamAppender)
                .map(OutputStreamAppender.class::cast)
                .findFirst().orElse(null);
        assertThat(outputAppender, notNullValue());

        Encoder<ILoggingEvent> encoder = outputAppender.getEncoder();
        assertThat(encoder, instanceOf(LayoutWrappingEncoder.class));
        Layout<ILoggingEvent> layout = ((LayoutWrappingEncoder<ILoggingEvent>) encoder).getLayout();
        assertThat(layout, instanceOf(CustomLogstashLayout.class));

        //check adding custom provider
        JsonProviders<ILoggingEvent> providers = ((CustomLogstashLayout) layout).getProviders();
        assertThat(providers.getProviders()
                        .stream()
                        .map(Object::getClass)
                        .map(Class::getName)
                        .filter(provider -> provider.equals(CUSTOM_PROVIDER_NAME))
                        .findAny()
                        .orElse(null),
                notNullValue());
        return (CustomLogstashLayout) layout;
    }

    private static ZoneOffset getSystemDefaultOffset() {
        return ZoneOffset.systemDefault()
                .getRules()
                .getOffset(Instant.now());
    }

}