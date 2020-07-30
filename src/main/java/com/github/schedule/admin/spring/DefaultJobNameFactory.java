package com.github.schedule.admin.spring;

import com.github.schedule.admin.JobNameFactory;
import com.github.schedule.admin.TimedRunnable;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

/**
 * @author yuni[mn960mn@163.com]
 */
public class DefaultJobNameFactory implements JobNameFactory {

    @Override
    public String getJobName(Runnable runnable) {
        Runnable realRunnable = runnable;
        if (runnable instanceof TimedRunnable) {
            realRunnable = ((TimedRunnable) runnable).getDelegate();
        }
        if (realRunnable instanceof ScheduledMethodRunnable) {
            ScheduledMethodRunnable methodRunnable = (ScheduledMethodRunnable) realRunnable;
            return methodRunnable.getTarget().getClass().getName() + "." + methodRunnable.getMethod().getName();
        }
        throw new IllegalArgumentException("UnSupport Runnable type of " + runnable.getClass());
    }
}
