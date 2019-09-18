package net.n2oapp.platform.quartz.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootApplication
@RunWith(SpringJUnit4ClassRunner.class)
@Import(TestConfig.class)
@SpringBootTest(classes = {QuartzConfigurationTest.class}, properties = "spring.liquibase.change-log=classpath:test-base-changelog.xml")
public class QuartzConfigurationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testTableContent() {
        List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM qrtz_job_details");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("job_name"), is("test_job"));

        result = jdbcTemplate.queryForList("SELECT * FROM qrtz_cron_triggers");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("trigger_name"), is("test_trigger"));
    }

    @Test
    public void testJob() throws InterruptedException {
        int before = TestJob.i;
        TimeUnit.SECONDS.sleep(1);
        assert TestJob.i > before;
    }
}
