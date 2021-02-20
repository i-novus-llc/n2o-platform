package net.n2oapp.platform.ms.autoconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@TestConfiguration
public class ApplicationInfoPrinterTestConfiguration {
    @Bean
    MemoryAppender memoryAppender(@Lazy ApplicationInfoPrinter applicationInfoPrinter) {
        Logger logger = (Logger) LoggerFactory.getLogger(ApplicationInfoPrinter.class);
        MemoryAppender memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.addAppender(memoryAppender);
        memoryAppender.start();
        return memoryAppender;
    }
}
