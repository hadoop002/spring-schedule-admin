package com.github.schedule.admin.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.config.Task;

import java.util.Date;

/**
 * @author yuni[mn960mn@163.com]
 */
@Setter
@Getter
public class JobDetail {

    private String jobName;

    private Task task;

    /**
     * 任务的状态，开启，还是关闭
     */
    private boolean started;

    private boolean isFixedRateTask;

    private boolean isCronTask;

    private Date lastExecuteTime;

    private Date lastCompletedTime;
}