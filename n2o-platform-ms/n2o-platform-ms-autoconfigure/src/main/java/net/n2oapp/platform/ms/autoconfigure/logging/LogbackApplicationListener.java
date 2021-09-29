package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.loki4j.logback.*;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * {@code LogbackApplicationListener} is a programmatic logging system configuration, faster alternative to a standard logback xml file configuration.
 * <p>
 * It adds a Loki appender over the existing logging configuration. Loki appender is only added if {@code n2o.ms.loki.enabled} setting is enabled.
 * Loki appender is added to the ROOT logger.
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
    public static final String LOKI_ENABLED_PROPERTY = "n2o.ms.loki.enabled";
    public static final String LOKI_URL_PROPERTY = "n2o.ms.loki.url";
    public static final String LOKI_URL_DEFAULT_VALUE = "http://loki:3100/loki/api/v1/push";
    public static final String APP_NAME_PROPERTY = "spring.application.name";
    public static final String DEFAULT_APP_NAME = "n2o-app";
    public static final String MESSAGE_PATTERN = "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr([${appName},%X{traceId},%X{spanId}]) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m %n%wEx";

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
        String lokiAppenderEnabled = env.getProperty(LOKI_ENABLED_PROPERTY);
        if (lokiAppenderEnabled != null && lokiAppenderEnabled.equals("true")) {
            String lokiUrl = resolveEnvProperty(env, LOKI_URL_PROPERTY, LOKI_URL_DEFAULT_VALUE);
            String appName = resolveEnvProperty(env, APP_NAME_PROPERTY, DEFAULT_APP_NAME);
            configureLokiAppender(lokiUrl, appName);
        }
    }

    private void configureLokiAppender(String lokiUrl, String appName) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        Loki4jAppender loki4jAppender = new Loki4jAppender();
        HttpSender httpSender = new JavaHttpSender();
        httpSender.setUrl(lokiUrl);
        loki4jAppender.setHttp(httpSender);
        JsonEncoder encoder = new JsonEncoder();
        AbstractLoki4jEncoder.LabelCfg labelCfg = new AbstractLoki4jEncoder.LabelCfg();
        String hostname = lc.getProperty("hostname");
        labelCfg.setPattern("app=" + appName + ",host=" + hostname);
        encoder.setLabel(labelCfg);
        AbstractLoki4jEncoder.MessageCfg messageCfg = new AbstractLoki4jEncoder.MessageCfg();
        String messagePatternResolved = MESSAGE_PATTERN.replace("${appName}", appName).replace("${PID}", String.valueOf(ProcessHandle.current().pid()));
        messageCfg.setPattern(messagePatternResolved);
        encoder.setMessage(messageCfg);
        encoder.setSortByTime(true);
        encoder.setContext(lc);
        loki4jAppender.setFormat(encoder);
        loki4jAppender.setContext(lc);
        loki4jAppender.start();

        root.addAppender(loki4jAppender);
    }

    private String resolveEnvProperty(ConfigurableEnvironment env, String propertyKey, String defaultValue) {
        String appName = env.getProperty(propertyKey);
        if (!StringUtils.hasLength(appName))
            appName = defaultValue;
        return appName;
    }
}
