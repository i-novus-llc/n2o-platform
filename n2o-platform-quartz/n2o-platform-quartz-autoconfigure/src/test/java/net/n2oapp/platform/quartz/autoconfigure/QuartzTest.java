package net.n2oapp.platform.quartz.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootApplication
@Import(QuartzTestConfiguration.class)
@SpringBootTest(classes = {QuartzTest.class})
@TestPropertySource("classpath:test.properties")
public class QuartzTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Тест загрузки джоба и триггера в БД
     */
    @Test
    public void testTableContent() {
        List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM qrtz_job_details");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("job_name"), is("test_job"));

        result = jdbcTemplate.queryForList("SELECT * FROM qrtz_cron_triggers");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("trigger_name"), is("test_trigger"));
    }

    /**
     * Тест выполнения джоба и передачи объекта через контекст
     */
    @Test
    public void testJob() throws InterruptedException {
        int before = TestJob.i;
        TimeUnit.SECONDS.sleep(1);
        assertThat(TestJob.i > before, is(true));
        assertThat(TestJob.context, is("test_context"));
    }
}
