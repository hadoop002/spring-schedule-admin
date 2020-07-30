package com.github.schedule.admin;

import org.springframework.scheduling.config.Task;

/**
 * @author yuni[mn960mn@163.com]
 */
@FunctionalInterface
public interface JobNameFactory {

    /**
     * 获取job名称
     *
     * @param runnable
     * @return
     */
    String getJobName(Runnable runnable);

    default String getJobName(Task task) {
        return getJobName(task.getRunnable());
    }
}