package net.n2oapp.platform.quartz.autoconfigure;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class TestJob implements Job {

    static int i = 0;

    @Override
    public void execute(JobExecutionContext context) {
        i++;
    }
}
