package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.github.loki4j.logback.*;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.nio.charset.Charset;

import static java.util.Objects.nonNull;
import static net.n2oapp.platform.ms.autoconfigure.logging.LoggingProperties.LOKI_APPENDER_NAME;

/**
 * {@code LogbackApplicationListener} is a programmatic logging system configuration, faster alternative to a standard logback xml file configuration.
 * <p>
 * It adds a Loki appender over the existing logging configuration. Loki appender is only added if {@code n2o.ms.loki.enabled} setting is enabled.
 * Loki appender is added to the ROOT logger.
 * <p>
 * It also replaces layout and encoder all appenders specified in the settings({@code n2o.ms.logging.json.appender_names}) for printing logs in Json format.
 * This only applies by setting {@code n2o.ms.logging.json.enabled}.
 * <p>
 * {@code LogbackApplicationListener} is spring factory class that works during start of the application in two phases:
 * <ol>
 * <li> After preparing the application environment by the {@code ApplicationEnvironmentPreparedEvent}.
 * Used for compatibility with standard spring boot logging system configuration.</li>
 * <li> After initializing(or reinitializing) the Spring context.
 * This phase is used for compatibility with the spring cloud bootstrap configuration.</li>
 * </ol>
 *
 * @author RMakhmutov
 * @since 09.09.2021
 */
@Configuration(proxyBeanMethods = false)
public class LogbackApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>,
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 20;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        configure(event.getEnvironment());
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        configure(applicationContext.getEnvironment());
    }

    private void configure(ConfigurableEnvironment env) {
        LoggingProperties properties = new LoggingProperties(env);
        if (Boolean.TRUE.equals(properties.getJsonEnabled()))
            configureJsonFormat(properties);
        if (Boolean.TRUE.equals(properties.getLokiEnabled()))
            configureLokiAppender(properties);
    }

    private void configureJsonFormat(LoggingProperties loggingProperties) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (nonNull(logger)) {
            for (String appenderName : loggingProperties.getJsonAppenderNames()) {
                OutputStreamAppender<ILoggingEvent> appender = (OutputStreamAppender<ILoggingEvent>) logger.getAppender(appenderName);
                if (nonNull(appender)) {
                    if (appender.isStarted()) appender.stop();
                    CustomLogstashLayout layout = new CustomLogstashLayout(lc, loggingProperties);
                    LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
                    encoder.setLayout(layout);
                    encoder.setContext(lc);
                    encoder.setCharset(Charset.defaultCharset());
                    appender.setEncoder(encoder);
                    layout.start();
                    encoder.start();
                    appender.start();
                }
            }
        }
    }

    private void configureLokiAppender(LoggingProperties properties) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        Loki4jAppender loki4jAppender = (Loki4jAppender) root.getAppender(LOKI_APPENDER_NAME);
        if (loki4jAppender == null) {
            loki4jAppender = new Loki4jAppender();
            loki4jAppender.setName(LOKI_APPENDER_NAME);
            root.addAppender(loki4jAppender);
        } else loki4jAppender.stop();
        configureLokiAppender(loki4jAppender, properties, lc);
        loki4jAppender.start();
    }

    private void configureLokiAppender(Loki4jAppender loki4jAppender, LoggingProperties properties, LoggerContext lc) {
        JavaHttpSender httpSender = new JavaHttpSender();
        httpSender.setUrl(properties.getLokiUrl());
        loki4jAppender.setHttp(httpSender);

        AbstractLoki4jEncoder.LabelCfg labelCfg = new AbstractLoki4jEncoder.LabelCfg();
        String hostname = properties.getHostname();
        labelCfg.setPattern("app=" + properties.getAppName() + ",host=" + hostname);

        AbstractLoki4jEncoder.MessageCfg messageCfg = new AbstractLoki4jEncoder.MessageCfg();
        String messagePatternResolved = properties.getMessagePattern().replace("${appName}", properties.getAppName()).replace("${PID}", String.valueOf(ProcessHandle.current().pid()));
        messageCfg.setPattern(messagePatternResolved);

        JsonEncoder encoder = properties.getJsonEnabled()
                ? new CustomLoki4jJsonEncoder(lc, properties)
                : new JsonEncoder();

        encoder.setLabel(labelCfg);
        encoder.setMessage(messageCfg);
        encoder.setSortByTime(true);
        encoder.setContext(lc);
        loki4jAppender.setFormat(encoder);
        loki4jAppender.setContext(lc);
    }
}