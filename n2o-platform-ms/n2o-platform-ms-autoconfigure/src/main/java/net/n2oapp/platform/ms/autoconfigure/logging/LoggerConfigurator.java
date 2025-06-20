package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.github.loki4j.logback.JavaHttpSender;
import com.github.loki4j.logback.Loki4jAppender;
import com.github.loki4j.logback.PipelineConfigAppenderBase;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.nio.charset.Charset;

import static java.util.Objects.nonNull;
import static net.n2oapp.platform.ms.autoconfigure.logging.LoggingProperties.LABEL_PATTERN;
import static net.n2oapp.platform.ms.autoconfigure.logging.LoggingProperties.LOKI_APPENDER_NAME;

public class LoggerConfigurator {

    private final LoggingProperties properties;

    public LoggerConfigurator(LoggingProperties properties) {
        this.properties = properties;
    }

    public LoggerConfigurator(ConfigurableEnvironment env) {
        this.properties = new LoggingProperties(env);
    }

    /**
     * Configure json format with CustomLogstashLayout for root logger OutputStreamAppender using LoggingProperties
     */
    public void configureJsonFormatIfRequired() {
        if (Boolean.TRUE.equals(properties.getJsonEnabled())) {
            configureJsonFormat();
        }
    }

    private void configureJsonFormat() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (nonNull(logger)) {
            for (String appenderName : properties.getJsonAppenderNames()) {
                OutputStreamAppender<ILoggingEvent> appender = (OutputStreamAppender<ILoggingEvent>) logger.getAppender(appenderName);
                if (nonNull(appender)) {
                    if (appender.isStarted()) appender.stop();
                    CustomLogstashLayout layout = new CustomLogstashLayout(lc, properties);
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

    /**
     * Configure Loki4jAppender for root logger using LoggingProperties
     */
    public void configureLokiAppenderIfRequired() {
        if (Boolean.TRUE.equals(properties.getLokiEnabled())) {
            configureLokiAppender();
        }
    }

    private void configureLokiAppender() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        Loki4jAppender loki4jAppender = (Loki4jAppender) root.getAppender(LOKI_APPENDER_NAME);
        if (loki4jAppender == null) {
            loki4jAppender = new Loki4jAppender();
            loki4jAppender.setName(LOKI_APPENDER_NAME);
            root.addAppender(loki4jAppender);
        } else {
            loki4jAppender.stop();
        }
        configureLokiAppender(loki4jAppender, lc);
        loki4jAppender.start();
    }

    private void configureLokiAppender(Loki4jAppender loki4jAppender, LoggerContext lc) {
        loki4jAppender.setHttp(getHttpConfig());
        String labels = String.format(LABEL_PATTERN, properties.getAppName(), properties.getHostname());
        loki4jAppender.setLabels(labels);
        loki4jAppender.setMessage(getLayout(lc));
        loki4jAppender.setContext(lc);
        loki4jAppender.setBatch(configureBatch());
    }

    private PipelineConfigAppenderBase.BatchCfg configureBatch() {
        PipelineConfigAppenderBase.BatchCfg batchCfg = new PipelineConfigAppenderBase.BatchCfg();
        batchCfg.setMaxItems(properties.getLokiBatchSize());
        return batchCfg;
    }

    private PipelineConfigAppenderBase.HttpCfg getHttpConfig() {
        PipelineConfigAppenderBase.HttpCfg httpCfg = new PipelineConfigAppenderBase.HttpCfg();
        httpCfg.setUrl(properties.getLokiUrl());
        httpCfg.setSender(new JavaHttpSender());
        return httpCfg;
    }

    private Layout<ILoggingEvent> getLayout(LoggerContext lc) {
        Layout<ILoggingEvent> layout;
        if (properties.getJsonEnabled()) {
            layout = new CustomLogstashLayout(lc, properties);
        } else {
            PatternLayout patternLayout = new PatternLayout();
            patternLayout.setPattern(properties.getResolvedMessagePattern());
            patternLayout.setContext(lc);
            layout = patternLayout;
        }
        return layout;
    }

}
