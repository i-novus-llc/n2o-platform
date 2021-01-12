package net.n2oapp.platform.ms.autoconfigure.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import org.springframework.util.StringUtils;

/**
 * Remove all line separator and add one to the end
 */
public class OneLinePatternLayout extends PatternLayout {
    @Override
    protected String writeLoopOnConverters(ILoggingEvent event) {
        String log = super.writeLoopOnConverters(event);
        log = StringUtils.replace(log, CoreConstants.LINE_SEPARATOR, " ");
        log = StringUtils.replace(log, "\n", " ");
        log = StringUtils.replace(log, "\r", " ");
        return log + CoreConstants.LINE_SEPARATOR;
    }
}