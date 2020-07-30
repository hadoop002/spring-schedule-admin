package com.github.schedule.admin.spring;


import com.github.schedule.admin.JobNameFactory;
import com.github.schedule.admin.TimedRunnable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author yuni[mn960mn@163.com]
 */
public class ManagedTaskScheduler implements TaskScheduler {

    private final TaskScheduler delegate;

    private final JobNameFactory jobNameFactory;

    private final Map<String, ManagedTask> managedTaskMap = new ConcurrentHashMap<>();

    public ManagedTaskScheduler(JobNameFactory jobNameFactory, TaskScheduler delegate) {
        this.jobNameFactory = jobNameFactory;
        this.delegate = delegate;
    }

    public ManagedTaskScheduler(JobNameFactory jobNameFactory, ScheduledExecutorService scheduledExecutor) {
        this.jobNameFactory = jobNameFactory;
        this.delegate = new ConcurrentTaskScheduler(scheduledExecutor);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        TimedRunnable timedRunnable = decorateRunnable(task);
        return doSchedule(timedRunnable, delegate.schedule(timedRunnable, trigger));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        TimedRunnable timedRunnable = decorateRunnable(task);
        return doSchedule(timedRunnable, delegate.schedule(timedRunnable, startTime));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        TimedRunnable timedRunnable = decorateRunnable(task);
        return doSchedule(timedRunnable, delegate.scheduleAtFixedRate(timedRunnable, startTime, period));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        TimedRunnable timedRunnable = decorateRunnable(task);
        return doSchedule(timedRunnable, delegate.scheduleAtFixedRate(timedRunnable, period));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        TimedRunnable timedRunnable = decorateRunnable(task);
        return doSchedule(timedRunnable, delegate.scheduleWithFixedDelay(timedRunnable, startTime, delay));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        TimedRunnable timedRunnable = decorateRunnable(task);
        return doSchedule(timedRunnable, delegate.scheduleWithFixedDelay(timedRunnable, delay));
    }

    protected TimedRunnable decorateRunnable(Runnable runnable) {
        return new TimedRunnable(runnable);
    }

    protected ScheduledFuture<?> doSchedule(TimedRunnable runnable, ScheduledFuture<?> scheduledFuture) {
        ManagedTask managedTask = new ManagedTask(jobNameFactory.getJobName(runnable), runnable, scheduledFuture);
        managedTaskMap.put(managedTask.getJobName(), managedTask);
        return scheduledFuture;
    }

    public boolean stopJob(String jobName) {
        ManagedTask managedTask = managedTaskMap.get(jobName);
        if (managedTask != null) {
            synchronized (managedTask) {
                try {
                    return managedTask.stop();
                } finally {
                    managedTaskMap.remove(jobName);
                }
            }
        }
        return false;
    }

    public void runJob(String jobName) {
        ManagedTask managedTask = managedTaskMap.get(jobName);
        if (managedTask != null) {
            synchronized (managedTask) {
                managedTask.getTimedRunnable().getDelegate().run();
            }
        }
    }

    public TimedRunnable getTimedRunnable(String jobName) {
        ManagedTask managedTask = managedTaskMap.get(jobName);
        if (managedTask != null) {
            return managedTask.getTimedRunnable();
        }
        return null;
    }

    public boolean jobIsStarted(Runnable runnable) {
        return jobIsStarted(jobNameFactory.getJobName(runnable));
    }

    public boolean jobIsStarted(String jobName) {
        return managedTaskMap.containsKey(jobName);
    }
}