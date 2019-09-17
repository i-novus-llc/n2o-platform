package net.n2oapp.platform.quartz.autoconfigure;

import org.quartz.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.quartz.CronScheduleBuilder.cronSchedule;

@TestConfiguration
@AutoConfigureAfter(LiquibaseAutoConfiguration.class)
public class TestConfig {

    private static final String EVERY_SECOND = "* * * ? * *";

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob().ofType(TestJob.class)
                .storeDurably()
                .withIdentity("test_job")
                .usingJobData(new JobDataMap())
                .build();
    }

    @Bean
    public Trigger trigger(JobDetail job) {
        return TriggerBuilder.newTrigger().forJob(job)
                .withIdentity("test_trigger")
                .withSchedule(cronSchedule(EVERY_SECOND))
                .build();
    }
}
