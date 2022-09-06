package net.n2oapp.platform.quartz.autoconfigure;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;

@TestConfiguration
@AutoConfigureAfter(LiquibaseAutoConfiguration.class)
public class QuartzTestConfiguration {

    @Value("${quartz.test.cron-expression}")
    private String cronExpression;

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
                .withSchedule(cronSchedule(cronExpression))
                .build();
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerContextCustomizer() {
        return (schedulerFactoryBean) -> {
            schedulerFactoryBean.setSchedulerContextAsMap(mapOf("context", "test_context"));
        };
    }

    public static <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
