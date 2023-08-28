package com.blueskies.springbatch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {
        log.info("Before Job {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Params {}", jobExecution.getJobParameters());
        log.info("Job Exec Context {}", jobExecution.getExecutionContext());
        jobExecution.getExecutionContext().put("jec","jec value");
    }

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        log.info("After Job {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Params {}", jobExecution.getJobParameters());
        log.info("Job Exec Context {}", jobExecution.getExecutionContext());
    }
}
