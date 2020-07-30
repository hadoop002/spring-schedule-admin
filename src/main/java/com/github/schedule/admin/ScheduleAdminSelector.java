package com.github.schedule.admin;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author yuni[mn960mn@163.com]
 */
public class ScheduleAdminSelector implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (adminEnabled()) {
            registry.registerBeanDefinition(ScheduleAdminAutoConfiguration.class.getName(), new RootBeanDefinition(ScheduleAdminAutoConfiguration.class));
        }
    }

    protected boolean adminEnabled() {
        return environment.getProperty(EnvironmentConstants.SCHEDULE_ADMIN_ENABLED, Boolean.class, true);
    }
}
