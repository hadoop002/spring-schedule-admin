package com.github.schedule.admin.spring.web;

import com.github.schedule.admin.spring.ManagedSchedulingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuni[mn960mn@163.com]
 */
@RequestMapping("/job")
public class ScheduleAdminController {

    public static final String RESPONSE = "success";

    @Autowired
    private ManagedSchedulingConfigurer managedSchedulingConfigurer;

    @ResponseBody
    @GetMapping("/getList")
    public List<JobModel> getJobs() {
        return managedSchedulingConfigurer.getJobDetailList().stream().map(JobModel::new).collect(Collectors.toList());
    }

    @ResponseBody
    @PostMapping("/start")
    public String startJob(@RequestParam("jobName") String jobName) {
        managedSchedulingConfigurer.startJob(jobName);
        return RESPONSE;
    }

    @ResponseBody
    @PostMapping("/stop")
    public String stopJob(@RequestParam("jobName") String jobName) {
        managedSchedulingConfigurer.stopJob(jobName);
        return RESPONSE;
    }

    @ResponseBody
    @PostMapping("/run")
    public String runJob(@RequestParam("jobName") String jobName) {
        managedSchedulingConfigurer.runJob(jobName);
        return RESPONSE;
    }
}
