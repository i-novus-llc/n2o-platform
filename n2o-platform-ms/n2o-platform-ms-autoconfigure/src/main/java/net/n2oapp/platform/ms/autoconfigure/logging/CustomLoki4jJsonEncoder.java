package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.loki4j.logback.JsonEncoder;
import net.logstash.logback.layout.LogstashLayout;

/**
 * Custom Loki4j JsonEncoder for logging in JSON format
 */
public class CustomLoki4jJsonEncoder extends JsonEncoder {

    private final LogstashLayout layout;

    public CustomLoki4jJsonEncoder(LoggerContext lc, LoggingProperties properties) {
        this.layout = new CustomLogstashLayout(lc, properties);
    }

    @Override
    public void start() {
        super.start();
        layout.start();
    }

    @Override
    public String eventToMessage(ILoggingEvent e) {
        return layout.doLayout(e);
    }
}