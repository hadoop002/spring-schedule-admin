package com.github.schedule.admin.spring;

import com.github.schedule.admin.EnvironmentConstants;
import com.github.schedule.admin.JobNameFactory;
import com.github.schedule.admin.TimedRunnable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.Task;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author yuni[mn960mn@163.com]
 */
public class ManagedSchedulingConfigurer implements SchedulingConfigurer, ApplicationContextAware, EnvironmentAware, DisposableBean, Ordered {

    private ScheduledTaskRegistrar taskRegistrar;

    private ManagedTaskScheduler managedTaskScheduler;

    private Environment environment;

    private ApplicationContext applicationContext;

    private ScheduledExecutorService scheduledExecutorService;

    private final Set<ScheduledTask> scheduledTasks = new LinkedHashSet<>();

    private final JobNameFactory jobNameFactory;

    public ManagedSchedulingConfigurer(JobNameFactory jobNameFactory) {
        this.jobNameFactory = jobNameFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        initManagedTaskScheduler();
        taskRegistrar.setTaskScheduler(this.managedTaskScheduler);
        this.taskRegistrar = taskRegistrar;
    }

    /**
     * 优先用用户自定义的bean
     */
    protected void initManagedTaskScheduler() {
        if (environment.containsProperty(EnvironmentConstants.TASKSCHEDULER_BEANNAME)) {
            String beanName = environment.getProperty(EnvironmentConstants.TASKSCHEDULER_BEANNAME);
            if (applicationContext.containsBean(beanName)) {
                this.managedTaskScheduler = new ManagedTaskScheduler(this.jobNameFactory, applicationContext.getBean(beanName, TaskScheduler.class));
                return;
            }
        }

        if (environment.containsProperty(EnvironmentConstants.SCHEDULED_EXECUTOR_BEANNAME)) {
            String beanName = environment.getProperty(EnvironmentConstants.SCHEDULED_EXECUTOR_BEANNAME);
            if (applicationContext.containsBean(beanName)) {
                this.managedTaskScheduler = new ManagedTaskScheduler(this.jobNameFactory, applicationContext.getBean(beanName, ScheduledExecutorService.class));
                return;
            }
        }

        if (this.managedTaskScheduler == null) {
            this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            this.managedTaskScheduler = new ManagedTaskScheduler(this.jobNameFactory, this.scheduledExecutorService);
        }
    }

    public List<JobDetail> getJobDetailList() {
        List<JobDetail> result = new ArrayList<>();
        result.addAll(processCronTaskToJobDetail(taskRegistrar.getCronTaskList()));
        result.addAll(processFixedRateTaskToJobDetail(taskRegistrar.getFixedRateTaskList()));
        result.addAll(processFixedDelayTaskToJobDetail(taskRegistrar.getFixedDelayTaskList()));
        return result;
    }

    protected List<JobDetail> processFixedRateTaskToJobDetail(List<IntervalTask> tasks) {
        return tasks.stream().map(task -> {
            JobDetail jobDetail = new JobDetail();
            jobDetail.setJobName(jobNameFactory.getJobName(task));
            jobDetail.setTask(task);
            jobDetail.setStarted(managedTaskScheduler.jobIsStarted(task.getRunnable()));
            jobDetail.setCronTask(false);
            jobDetail.setFixedRateTask(true);
            TimedRunnable timedRunnable = managedTaskScheduler.getTimedRunnable(jobDetail.getJobName());
            if (timedRunnable != null) {
                jobDetail.setLastExecuteTime(timedRunnable.getLastExecuteTime());
                jobDetail.setLastCompletedTime(timedRunnable.getLastCompletedTime());
            }
            return jobDetail;
        }).collect(Collectors.toList());
    }

    protected List<JobDetail> processFixedDelayTaskToJobDetail(List<IntervalTask> tasks) {
        return tasks.stream().map(task -> {
            JobDetail jobDetail = new JobDetail();
            jobDetail.setJobName(jobNameFactory.getJobName(task));
            jobDetail.setTask(task);
            jobDetail.setStarted(managedTaskScheduler.jobIsStarted(task.getRunnable()));
            TimedRunnable timedRunnable = managedTaskScheduler.getTimedRunnable(jobDetail.getJobName());
            if (timedRunnable != null) {
                jobDetail.setLastExecuteTime(timedRunnable.getLastExecuteTime());
                jobDetail.setLastCompletedTime(timedRunnable.getLastCompletedTime());
            }
            return jobDetail;
        }).collect(Collectors.toList());
    }

    protected List<JobDetail> processCronTaskToJobDetail(List<CronTask> tasks) {
        return tasks.stream().map(task -> {
            JobDetail jobDetail = new JobDetail();
            jobDetail.setJobName(jobNameFactory.getJobName(task));
            jobDetail.setTask(task);
            jobDetail.setStarted(managedTaskScheduler.jobIsStarted(task.getRunnable()));
            jobDetail.setCronTask(true);
            TimedRunnable timedRunnable = managedTaskScheduler.getTimedRunnable(jobDetail.getJobName());
            if (timedRunnable != null) {
                jobDetail.setLastExecuteTime(timedRunnable.getLastExecuteTime());
                jobDetail.setLastCompletedTime(timedRunnable.getLastCompletedTime());
            }
            return jobDetail;
        }).collect(Collectors.toList());
    }

    public void runJob(String name) {
        managedTaskScheduler.runJob(name);
    }

    public boolean stopJob(String name) {
        return managedTaskScheduler.stopJob(name);
    }

    public synchronized boolean startJob(String name) {
        CronTask cronTask = getTask(taskRegistrar.getCronTaskList(), name);
        if (cronTask != null) {
            saveForCancel(taskRegistrar.scheduleCronTask(cronTask));
            return true;
        }

        IntervalTask intervalTask = getTask(taskRegistrar.getFixedRateTaskList(), name);
        if (intervalTask != null) {
            saveForCancel(taskRegistrar.scheduleFixedRateTask(intervalTask));
            return true;
        }

        intervalTask = getTask(taskRegistrar.getFixedDelayTaskList(), name);
        if (intervalTask != null) {
            saveForCancel(taskRegistrar.scheduleFixedDelayTask(intervalTask));
            return true;
        }

        return false;
    }

    protected void saveForCancel(ScheduledTask scheduledTask) {
        scheduledTasks.add(scheduledTask);
    }

    private <T extends Task> T getTask(List<T> tasks, String name) {
        return tasks.stream().filter(task -> jobNameFactory.getJobName(task).equals(name)).findAny().orElse(null);
    }

    @Override
    public void destroy() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        scheduledTasks.forEach(ScheduledTask::cancel);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
