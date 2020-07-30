package com.github.schedule.admin.spring;

import com.github.schedule.admin.TimedRunnable;
import lombok.Getter;

import java.util.concurrent.ScheduledFuture;

/**
 * @author yuni[mn960mn@163.com]
 */
@Getter
public class ManagedTask {

    private final String jobName;

    private final TimedRunnable timedRunnable;

    private final ScheduledFuture<?> scheduledFuture;

    public ManagedTask(String jobName, TimedRunnable timedRunnable, ScheduledFuture<?> scheduledFuture) {
        this.jobName = jobName;
        this.timedRunnable = timedRunnable;
        this.scheduledFuture = scheduledFuture;
    }

    public String getJobName() {
        return this.jobName;
    }

    public boolean stop() {
        return stop(false);
    }

    public boolean stop(boolean mayInterruptIfRunning) {
        return scheduledFuture.cancel(mayInterruptIfRunning);
    }
}