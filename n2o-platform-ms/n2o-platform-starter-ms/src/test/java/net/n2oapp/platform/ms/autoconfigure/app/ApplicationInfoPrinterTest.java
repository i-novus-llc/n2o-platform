package net.n2oapp.platform.ms.autoconfigure.app;

import ch.qos.logback.classic.Level;
import net.n2oapp.platform.ms.autoconfigure.ApplicationInfoPrinterTestConfiguration;
import net.n2oapp.platform.ms.autoconfigure.MemoryAppender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationInfoPrinterTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@Import(ApplicationInfoPrinterTestConfiguration.class)
public class ApplicationInfoPrinterTest {

    @Autowired
    private MemoryAppender memoryAppender;

    @Test
    public void testApplicationInfoPrinter() {
        assertThat(memoryAppender.contains("Application info:", Level.INFO), is(true));
        assertThat(memoryAppender.contains("groupId: net.n2oapp.platform", Level.INFO), is(true));
        assertThat(memoryAppender.contains("artifactId: test", Level.INFO), is(true));
        assertThat(memoryAppender.contains("version: 0.0.1-SNAPSHOT", Level.INFO), is(true));
        assertThat(memoryAppender.contains("built at: 2021-01-01T00:00:00Z", Level.INFO), is(true));
    }
}
