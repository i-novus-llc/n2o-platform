package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.decorate.PrettyPrintingJsonGeneratorDecorator;
import net.logstash.logback.layout.LogstashLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom LogstashLayout for logging in JSON format
 */
public class CustomLogstashLayout extends LogstashLayout {

    public CustomLogstashLayout(LoggerContext lc, LoggingProperties properties) {
        super();
        this.setContext(lc);
        this.setLineSeparator("SYSTEM");
        this.setMessageSplitRegex("SYSTEM");
        this.setTimestampPattern(properties.getJsonTimestampPattern());
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
    }

    private void excludeProviders(List<String> providerToExcludeNames) {
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