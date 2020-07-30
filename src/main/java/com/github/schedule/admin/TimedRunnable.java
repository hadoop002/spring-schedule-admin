package com.github.schedule.admin;

import lombok.Getter;

import java.util.Date;

/**
 * @author yuni[mn960mn@163.com]
 */
@Getter
public class TimedRunnable implements Runnable {

    private final Runnable delegate;

    /**
     * 最后一次执行时间
     */
    private Date lastExecuteTime;

    /**
     * 最后一次执行结束时间
     */
    private Date lastCompletedTime;

    public TimedRunnable(Runnable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void run() {
        lastExecuteTime = new Date();
        try {
            delegate.run();
        } finally {
            lastCompletedTime = new Date();
        }
    }
}
