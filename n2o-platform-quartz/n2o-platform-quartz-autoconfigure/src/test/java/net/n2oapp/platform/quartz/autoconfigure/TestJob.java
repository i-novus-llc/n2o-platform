package net.n2oapp.platform.quartz.autoconfigure;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

public class TestJob implements Job {

    static int i = Integer.MIN_VALUE;
    static String context = null;

    @Override
    public void execute(JobExecutionContext context) {
        if (TestJob.context == null) {
            try {
                TestJob.context = context.getScheduler().getContext().get("context").toString();
            } catch (SchedulerException e) {
                assert false;
            }
        }
        i++;
    }
}
