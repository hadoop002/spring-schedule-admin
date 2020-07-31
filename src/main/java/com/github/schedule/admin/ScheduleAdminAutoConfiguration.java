package com.github.schedule.admin;

import com.github.schedule.admin.spring.DefaultJobNameFactory;
import com.github.schedule.admin.spring.ManagedSchedulingConfigurer;
import com.github.schedule.admin.spring.web.ScheduleAdminController;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Bean;

/**
 * @author yuni[mn960mn@163.com]
 */
public class ScheduleAdminAutoConfiguration {

    @Bean
    public ManagedSchedulingConfigurer managedSchedulingConfigurer(BeanFactory beanFactory) {
        JobNameFactory jobNameFactory = null;
        try {
            jobNameFactory = beanFactory.getBean(JobNameFactory.class);
        } catch (NoSuchBeanDefinitionException e) {
            //ignore
        }
        return new ManagedSchedulingConfigurer(jobNameFactory == null ? new DefaultJobNameFactory() : jobNameFactory);
    }

    @Bean
    public ScheduleAdminController scheduleAdminController() {
        return new ScheduleAdminController();
    }
}
