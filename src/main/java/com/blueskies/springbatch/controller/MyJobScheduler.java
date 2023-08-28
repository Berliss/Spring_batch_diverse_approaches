package com.blueskies.springbatch.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

//@Service
public class MyJobScheduler {

//    @Autowired
    @Qualifier("firstJob")
    private Job taskletJob;

//    @Autowired
    private JobLauncher jobLauncher;

//    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void executeTask() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("currentTime", new JobParameter<>(String.valueOf(System.currentTimeMillis()), String.class) );

        JobParameters jobParameters = new JobParameters(params);

        JobExecution jobExecution = jobLauncher.run(taskletJob, jobParameters);
        System.out.println("Job ID -> " + jobExecution.getJobId());
    }
}
