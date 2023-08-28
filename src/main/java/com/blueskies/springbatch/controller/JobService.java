package com.blueskies.springbatch.controller;

import com.blueskies.springbatch.request.JobParamRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobService {

    @Autowired
    @Qualifier("firstJob")
    private Job taskletJob;
    @Autowired
    @Qualifier("mySecondJob")
    private Job chunkJob;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobOperator jobOperator;

    @Async
    public void startJob(String jobName, List<JobParamRequest> additionalParams) throws Exception {

        Map<String, JobParameter<?>> params = new HashMap<>();
        additionalParams.forEach(param -> params.put(param.paramKey(), new JobParameter<>(param.paramValue(), String.class)));
        params.put("currentTime", new JobParameter<>(String.valueOf(System.currentTimeMillis()), String.class));

        JobParameters jobParameters = new JobParameters(params);

        Job jobToLaunch = jobName.equals("firstJob") ? taskletJob : chunkJob;
        JobExecution jobExecution = jobLauncher.run(jobToLaunch, jobParameters);
        System.out.println("Job ID -> " + jobExecution.getJobId());
    }

    public void stopJob(long executionId) {
        try {
            jobOperator.stop(executionId);
        } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
            e.printStackTrace();
        }
    }
}
