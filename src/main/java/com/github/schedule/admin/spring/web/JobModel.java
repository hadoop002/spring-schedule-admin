package com.github.schedule.admin.spring.web;

import com.github.schedule.admin.spring.JobDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yuni[mn960mn@163.com]
 */
@Setter
@Getter
@NoArgsConstructor
public class JobModel {

    private String name;

    /**
     * 执行周期
     */
    private String period = "";

    private String initialDelay = "";

    private String status;

    private String nextTime = "";

    /**
     * 最近5次的执行时间
     */
    private List<String> nextTimes = new ArrayList<>();

    public JobModel(JobDetail jobDetail) {
        this.name = jobDetail.getJobName();
        this.status = jobDetail.isStarted() ? "运行中" : "已停止";
        Task task = jobDetail.getTask();
        if (task instanceof IntervalTask) {
            init(jobDetail, (IntervalTask) task);
        } else {
            init(jobDetail, (CronTask) task);

        }
    }

    protected void init(JobDetail jobDetail, IntervalTask intervalTask) {
        this.period = intervalTask.getInterval() + "(ms)";
        this.initialDelay = intervalTask.getInitialDelay() + "(ms)";
        if (!jobDetail.isStarted()) {
            return;
        }

        Date startDate;

        //按固定周期执行的任务
        if (jobDetail.isFixedRateTask() && jobDetail.getLastExecuteTime() != null) {
            startDate = jobDetail.getLastExecuteTime();

            for (int i = 1; i <= 5; i++) {
                startDate = plusMilliSeconds(startDate, intervalTask.getInterval());
                nextTimes.add(formatDate(startDate));
            }

            nextTime = nextTimes.get(0);
        }

        //按上次执行完时间固定执行
        if (!jobDetail.isFixedRateTask() && jobDetail.getLastCompletedTime() != null) {
            //如果当前任务正在执行，这里的最后完成时间是上次的最后完成时间
            startDate = jobDetail.getLastCompletedTime();

            //由于是按每次执行完之后固定时间执行，而每次执行的时间不知道，所以这里无法计算最近5次的执行时间
            nextTime = formatDate(plusMilliSeconds(startDate, intervalTask.getInterval()));
        }
    }

    protected void init(JobDetail jobDetail, CronTask cronTask) {
        this.period = cronTask.getExpression();
        if (!jobDetail.isStarted()) {
            return;
        }
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(this.period);
        Date date = jobDetail.getLastExecuteTime() == null ? new Date() : jobDetail.getLastExecuteTime();
        for (int i = 1; i <= 5; i++) {
            date = cronSequenceGenerator.next(date);
            nextTimes.add(formatDate(date));
        }
        nextTime = nextTimes.get(0);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private Date plusMilliSeconds(Date date, long milliseconds) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = date.toInstant().atZone(zoneId).toLocalDateTime();
        return Date.from(dateTime.plus(milliseconds, ChronoUnit.MILLIS).atZone(zoneId).toInstant());
    }
}
