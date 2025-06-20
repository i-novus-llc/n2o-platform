package net.n2oapp.platform.ms.autoconfigure.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.n2oapp.platform.ms.autoconfigure.MemoryAppender;
import net.n2oapp.platform.ms.autoconfigure.logging.LoggerConfigurator;
import net.n2oapp.platform.ms.autoconfigure.logging.LoggingProperties;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = LokiAppenderTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "n2o.ms.loki.enabled=false",
                "n2o.ms.loki.batch.size=1",
                "spring.application.name=loki-appender-test"
        })
@EnableAutoConfiguration
public class LokiAppenderTest {

    private final static String APP_NAME = "loki-appender-test";
    private final static Logger LOGGER = LoggerFactory.getLogger(LokiAppenderTest.class);
    private final static GenericContainer LOKI_CONTAINER = new GenericContainer(DockerImageName.parse("grafana/loki:2.3.0"))
            .waitingFor(Wait.forLogMessage("(.)*Loki started(.)*", 1)
                    .withStartupTimeout(Duration.ofSeconds(60)))
            .withExposedPorts(3100);
    private final static String TEST_MESSAGE = "test message";
    private final static String SECOND_TEST_MESSAGE = "second message";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static MemoryAppender MEMORY_APPENDER;

    @Autowired
    private ConfigurableEnvironment environment;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        LOKI_CONTAINER.start();
        registry.add("n2o.ms.loki.url", () -> LokiAppenderTest.getLokiUrl() + "/loki/api/v1/push");
        registry.add("n2o.ms.loki.enabled", () -> true);
    }

    private static String getLokiUrl() {
        return String.format("http://%s:%d", LOKI_CONTAINER.getHost(), LOKI_CONTAINER.getFirstMappedPort());
    }

    @BeforeEach
    public void beforeEach() {
        Awaitility.setDefaultPollInterval(100, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ofMillis(500));
        Awaitility.setDefaultTimeout(Duration.ofSeconds(10));
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ROOT");
        MEMORY_APPENDER = new MemoryAppender();
        MEMORY_APPENDER.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.addAppender(MEMORY_APPENDER);
        MEMORY_APPENDER.start();
        MEMORY_APPENDER.clear();
    }

    @Test
    void testLokiSendPlainText() {
        environment.getSystemProperties()
                .put("n2o.ms.logging.json.enabled", false);
        LoggingProperties properties = new LoggingProperties(environment);
        LoggerConfigurator loggerConfigurator = new LoggerConfigurator(properties);
        loggerConfigurator.configureLokiAppenderIfRequired();

        LOGGER.info(TEST_MESSAGE);
        String expectedLog = getExpectedPlainTextLog(properties);

        String actualLog = getLogFromLoki(properties, TEST_MESSAGE);
        Assertions.assertEquals(expectedLog, actualLog);
    }

    @Test
    void testLokiSendJson() {
        environment.getSystemProperties()
                .put("n2o.ms.logging.json.enabled", true);
        LoggingProperties properties = new LoggingProperties(environment);
        LoggerConfigurator loggerConfigurator = new LoggerConfigurator(properties);
        loggerConfigurator.configureLokiAppenderIfRequired();

        LOGGER.info(SECOND_TEST_MESSAGE);
        String actualLog = getLogFromLoki(properties, SECOND_TEST_MESSAGE);
        String expectedLog = getExpectedJsonTextLog(properties);
        Assertions.assertEquals(expectedLog, actualLog);
    }

    private String getExpectedJsonTextLog(LoggingProperties properties) {
        String expectedTimeStamp = getTimestampByMessage(SECOND_TEST_MESSAGE, properties.getJsonTimestampPattern());
        return String.format("{\"@timestamp\":\"%s\",\"message\":[\"%s\"],\"logger_name\":\"%s\",\"thread_name\":\"main\",\"level\":\"INFO\"}%n",
                expectedTimeStamp, SECOND_TEST_MESSAGE, LokiAppenderTest.class.getName());
    }

    private String getExpectedPlainTextLog(LoggingProperties properties) {
        String expectedTimeStamp = getTimestampByMessage(TEST_MESSAGE, "yyyy-MM-dd HH:mm:ss.SSS");
        return String.format("%s  INFO [%s,,] %s --- [           main] n.n.p.m.a.app.LokiAppenderTest           : %s %n",
                expectedTimeStamp,
                properties.getAppName(),
                new ApplicationPid(),
                TEST_MESSAGE
        );
    }

    private String getTimestampByMessage(String message, String timestampPattern) {
        ILoggingEvent event = MEMORY_APPENDER.findFirst(message);
        Assertions.assertNotNull(event);
        return LocalDateTime.ofInstant(event.getInstant(), ZoneOffset.systemDefault())
                .format(DateTimeFormatter.ofPattern(timestampPattern));
    }

    private String getLogFromLoki(LoggingProperties properties, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String query = "{app=\"" + APP_NAME + "\",host=\"" + properties.getHostname() + "\"} |= \"" + message + "\"";
        URI uri = UriComponentsBuilder.fromUriString(getLokiUrl() + "/loki/api/v1/query")
                .queryParam("query", query)
                .build()
                .toUri();
        ResponseEntity<String> response = Awaitility.await()
                .until(() -> restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class),
                        responseEntity -> responseEntity.getStatusCode().is2xxSuccessful());
        return getFirstLog(response);
    }

    private String getFirstLog(ResponseEntity<String> response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("data")
                    .get("result")
                    .get(0)
                    .get("values")
                    .get(0)
                    .get(1).asText();
        } catch (Exception e) {
            Assertions.fail("Can not get first log from Loki response: " + response.getBody(), e);
            throw new IllegalStateException(e);
        }
    }

}
