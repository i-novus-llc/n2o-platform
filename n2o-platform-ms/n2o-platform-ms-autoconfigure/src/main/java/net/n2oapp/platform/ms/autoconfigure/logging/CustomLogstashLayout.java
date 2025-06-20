package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.decorate.PrettyPrintingJsonGeneratorDecorator;
import net.logstash.logback.layout.LogstashLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom LogstashLayout for logging in JSON format
 */
public class CustomLogstashLayout extends LogstashLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CustomLogstashLayout.class);

    public CustomLogstashLayout(LoggerContext lc, LoggingProperties properties) {
        super();
        this.setContext(lc);
        this.setLineSeparator(properties.getJsonLineSeparator());
        this.setMessageSplitRegex(properties.getJsonMessageSplitRegex());
        this.setTimestampPattern(properties.getJsonTimestampPattern());
        if (properties.getJsonTimestampFieldName() != null)
            this.getFieldNames().setTimestamp(properties.getJsonTimestampFieldName());
        this.setIncludeTags(properties.getJsonIncludeTags());
        this.setIncludeContext(properties.getJsonIncludeContext());
        this.setIncludeCallerData(properties.getJsonIncludeCallerData());
        this.setIncludeMdc(properties.getJsonIncludeMdc());
        if (Boolean.TRUE.equals(properties.getJsonIncludeMdc())) {
            this.setIncludeMdcKeyNames(properties.getJsonMdcIncludeKeys());
            this.setExcludeMdcKeyNames(properties.getJsonMdcExcludeKeys());
        }
        if (Boolean.TRUE.equals(properties.getJsonPrettyPrint()))
            this.setJsonGeneratorDecorator(new PrettyPrintingJsonGeneratorDecorator());

        excludeProviders(properties.getJsonProviderExcludeNames());
        includeProviders(properties);
    }

    private void excludeProviders(List<String> providerToExcludeNames) {
        if (!CollectionUtils.isEmpty(providerToExcludeNames)) {
            JsonProviders<ILoggingEvent> providers = this.getProviders();
            List<JsonProvider<ILoggingEvent>> providersToExclude = new ArrayList<>();
            for (JsonProvider<ILoggingEvent> provider : providers.getProviders()) {
                Class<? extends JsonProvider> providerClass = provider.getClass();
                if (providerToExcludeNames.contains(providerClass.getName())) {
                    providersToExclude.add(provider);
                }
            }
            providersToExclude.forEach(providers::removeProvider);
        }
    }

    private void includeProviders(LoggingProperties properties) {
        if (!CollectionUtils.isEmpty(properties.getJsonProviderIncludeNames())) {
            JsonProviders<ILoggingEvent> providers = this.getProviders();
            for (String className : properties.getJsonProviderIncludeNames()) {
                JsonProvider<ILoggingEvent> jsonProvider = getJsonProvider(className);
                if (jsonProvider != null) {
                    providers.addProvider(jsonProvider);
                } else {
                    LOG.warn(className + " is not implementation of " + JsonProvider.class.getName());
                }
            }
        }
    }

    private JsonProvider<ILoggingEvent> getJsonProvider(String className) {
        Object newProvider = null;
        try {
            Class<?> providerClass = Class.forName(className);
            newProvider = providerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOG.warn("Not found class for provider name:" + className, e);
        }
        return newProvider instanceof JsonProvider ?
                (JsonProvider<ILoggingEvent>) newProvider
                : null;
    }

}