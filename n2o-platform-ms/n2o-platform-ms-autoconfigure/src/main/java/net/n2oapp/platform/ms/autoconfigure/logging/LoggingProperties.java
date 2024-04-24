package net.n2oapp.platform.ms.autoconfigure.logging;

import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class LoggingProperties {

    private static final String DEFAULT_HOST_NAME = "Unknown host";

    public static final String MESSAGE_PATTERN_PROPERTY = "n2o.ms.logging.message.pattern";
    public static final String MESSAGE_PATTERN_DEFAULT_VALUE = "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr([${appName},%X{traceId},%X{spanId}]) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m %n%wEx";

    //JSON logs properties
    public static final String LOGGING_JSON_FORMAT_ENABLED_PROPERTY = "n2o.ms.logging.json.enabled";

    public static final String LOGGING_JSON_TIMESTAMP_PATTERN_PROPERTY = "n2o.ms.logging.json.timestamp.pattern";
    public static final String LOGGING_JSON_TIMESTAMP_PATTERN_DEFAULT_VALUE = "yyyy-MM-dd' 'HH:mm:ss.SSS";
    private static final String LOGGING_JSON_TIMESTAMP_FIELD_NAME_PROPERTY = "n2o.ms.logging.json.timestamp.field_name";


    private static final String LOGGING_JSON_LINE_SEPARATOR_PROPERTY = "n2o.ms.logging.json.line_separator";
    private static final String LOGGING_JSON_LINE_SEPARATOR_DEFAULT_VALUE = "SYSTEM";

    private static final String LOGGING_JSON_MESSAGE_SPLIT_REGEX_PROPERTY = "n2o.ms.logging.json.message_split_regex";
    private static final String LOGGING_JSON_MESSAGE_SPLIT_REGEX_DEFAULT_VALUE = "SYSTEM";

    public static final String LOGGING_JSON_PRETTY_PRINT_PROPERTY = "n2o.ms.logging.json.pretty_print";
    private static final String LOGGING_JSON_INCLUDE_TAGS_PROPERTY = "n2o.ms.logging.json.include.tags";
    private static final String LOGGING_JSON_INCLUDE_CONTEXT_PROPERTY = "n2o.ms.logging.json.include.context";
    private static final String LOGGING_JSON_INCLUDE_CALLER_DATA_PROPERTY = "n2o.ms.logging.json.include.caller_data";

    public static final String LOGGING_JSON_APPENDER_NAMES_PROPERTY = "n2o.ms.logging.json.appender_names";
    public static final List<String> LOGGING_JSON_APPENDER_NAMES_DEFAULT_VALUE = List.of("CONSOLE", "FILE");

    public static final String LOGGING_JSON_MDC_INCLUDE_PROPERTY = "n2o.ms.logging.json.mdc.include";
    public static final String LOGGING_JSON_MDC_INCLUDE_KEYS_PROPERTY = "n2o.ms.logging.json.mdc.include_keys";
    public static final String LOGGING_JSON_MDC_EXCLUDE_KEYS_PROPERTY = "n2o.ms.logging.json.mdc.exclude_keys";

    public static final String LOGGING_JSON_PROVIDERS_INCLUDE_NAMES = "n2o.ms.logging.json.provider.include_names";
    public static final String LOGGING_JSON_PROVIDERS_EXCLUDE_NAMES = "n2o.ms.logging.json.provider.exclude_names";
    public static final List<String> LOGGING_JSON_PROVIDERS_EXCLUDE_NAMES_DEFAULT_VALUE = List.of("net.logstash.logback.composite.LogstashVersionJsonProvider",
            "net.logstash.logback.composite.loggingevent.LogLevelValueJsonProvider");

    //Loki4j properties
    public static final String LOKI_ENABLED_PROPERTY = "n2o.ms.loki.enabled";

    public static final String LOKI_URL_PROPERTY = "n2o.ms.loki.url";
    public static final String LOKI_URL_DEFAULT_VALUE = "http://loki:3100/loki/api/v1/push";

    public static final String LOKI_APPENDER_NAME = "LOKI_APPENDER";

    public static final String APP_NAME_PROPERTY = "spring.application.name";
    public static final String DEFAULT_APP_NAME = "n2o-app";

    private final String hostname;
    private final String appName;
    private final String messagePattern;

    private final Boolean jsonEnabled;
    private final String jsonTimestampPattern;
    private final String jsonTimestampFieldName;
    private final String jsonLineSeparator;
    private final String jsonMessageSplitRegex;
    private final Boolean jsonIncludeMdc;
    private final Boolean jsonPrettyPrint;
    private final Boolean jsonIncludeTags;
    private final Boolean jsonIncludeContext;
    private final Boolean jsonIncludeCallerData;
    private final List<String> jsonAppenderNames;
    private final List<String> jsonMdcExcludeKeys;
    private final List<String> jsonMdcIncludeKeys;
    private final List<String> jsonProviderIncludeNames;
    private final List<String> jsonProviderExcludeNames;

    private final Boolean lokiEnabled;
    private final String lokiUrl;

    public LoggingProperties(ConfigurableEnvironment env) {
        this.hostname = getOrDefaultHostName();
        this.appName = env.getProperty(APP_NAME_PROPERTY, DEFAULT_APP_NAME);
        this.messagePattern = env.getProperty(MESSAGE_PATTERN_PROPERTY, MESSAGE_PATTERN_DEFAULT_VALUE);
        this.jsonLineSeparator = env.getProperty(LOGGING_JSON_LINE_SEPARATOR_PROPERTY, LOGGING_JSON_LINE_SEPARATOR_DEFAULT_VALUE);
        this.jsonMessageSplitRegex = env.getProperty(LOGGING_JSON_MESSAGE_SPLIT_REGEX_PROPERTY, LOGGING_JSON_MESSAGE_SPLIT_REGEX_DEFAULT_VALUE);

        this.jsonEnabled = env.getProperty(LOGGING_JSON_FORMAT_ENABLED_PROPERTY, Boolean.class, Boolean.FALSE);
        this.jsonTimestampPattern = env.getProperty(LOGGING_JSON_TIMESTAMP_PATTERN_PROPERTY, LOGGING_JSON_TIMESTAMP_PATTERN_DEFAULT_VALUE);
        this.jsonTimestampFieldName = env.getProperty(LOGGING_JSON_TIMESTAMP_FIELD_NAME_PROPERTY);
        this.jsonIncludeMdc = env.getProperty(LOGGING_JSON_MDC_INCLUDE_PROPERTY, Boolean.class, Boolean.FALSE);
        this.jsonPrettyPrint = env.getProperty(LOGGING_JSON_PRETTY_PRINT_PROPERTY, Boolean.class, Boolean.FALSE);
        this.jsonIncludeTags = env.getProperty(LOGGING_JSON_INCLUDE_TAGS_PROPERTY, Boolean.class, Boolean.FALSE);
        this.jsonIncludeContext = env.getProperty(LOGGING_JSON_INCLUDE_CONTEXT_PROPERTY, Boolean.class, Boolean.FALSE);
        this.jsonIncludeCallerData = env.getProperty(LOGGING_JSON_INCLUDE_CALLER_DATA_PROPERTY, Boolean.class, Boolean.FALSE);
        this.jsonAppenderNames = env.getProperty(LOGGING_JSON_APPENDER_NAMES_PROPERTY, List.class, LOGGING_JSON_APPENDER_NAMES_DEFAULT_VALUE);
        this.jsonMdcExcludeKeys = env.getProperty(LOGGING_JSON_MDC_EXCLUDE_KEYS_PROPERTY, List.class, Collections.emptyList());
        this.jsonMdcIncludeKeys = env.getProperty(LOGGING_JSON_MDC_INCLUDE_KEYS_PROPERTY, List.class, Collections.emptyList());
        this.jsonProviderIncludeNames = env.getProperty(LOGGING_JSON_PROVIDERS_INCLUDE_NAMES, List.class, Collections.emptyList());
        this.jsonProviderExcludeNames = env.getProperty(LOGGING_JSON_PROVIDERS_EXCLUDE_NAMES, List.class, LOGGING_JSON_PROVIDERS_EXCLUDE_NAMES_DEFAULT_VALUE);

        this.lokiEnabled = env.getProperty(LOKI_ENABLED_PROPERTY, Boolean.class, Boolean.FALSE);
        this.lokiUrl = env.getProperty(LOKI_URL_PROPERTY, LOKI_URL_DEFAULT_VALUE);
    }

    public String getHostname() {
        return hostname;
    }

    public String getAppName() {
        return appName;
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public Boolean getJsonEnabled() {
        return jsonEnabled;
    }

    public String getJsonTimestampPattern() {
        return jsonTimestampPattern;
    }

    public String getJsonTimestampFieldName() {
        return jsonTimestampFieldName;
    }

    public String getJsonLineSeparator() {
        return jsonLineSeparator;
    }

    public String getJsonMessageSplitRegex() {
        return jsonMessageSplitRegex;
    }

    public Boolean getJsonIncludeMdc() {
        return jsonIncludeMdc;
    }

    public Boolean getJsonPrettyPrint() {
        return jsonPrettyPrint;
    }

    public Boolean getJsonIncludeTags() {
        return jsonIncludeTags;
    }

    public Boolean getJsonIncludeContext() {
        return jsonIncludeContext;
    }

    public Boolean getJsonIncludeCallerData() {
        return jsonIncludeCallerData;
    }

    public List<String> getJsonAppenderNames() {
        return jsonAppenderNames;
    }

    public List<String> getJsonMdcExcludeKeys() {
        return jsonMdcExcludeKeys;
    }

    public List<String> getJsonMdcIncludeKeys() {
        return jsonMdcIncludeKeys;
    }

    public List<String> getJsonProviderIncludeNames() {
        return jsonProviderIncludeNames;
    }

    public List<String> getJsonProviderExcludeNames() {
        return jsonProviderExcludeNames;
    }

    public Boolean getLokiEnabled() {
        return lokiEnabled;
    }

    public String getLokiUrl() {
        return lokiUrl;
    }

    private String getOrDefaultHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return DEFAULT_HOST_NAME;
        }
    }
}