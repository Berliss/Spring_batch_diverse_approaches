package com.blueskies.springbatch.tasklet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@AllArgsConstructor
public class SampleTaskletJob {

    private MyJobListener myJobListener;
    private JobRepository jobRepository;
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public Job firstJob(JobRepository jobRepository) {
        return new JobBuilder("Tasklet example job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(myJobListener)
                .start(fistStep())
                .next(secondStep())
                .build();
    }

    private Step fistStep() {
        return new StepBuilder("First Step", jobRepository)
                .tasklet(tasklet1(), platformTransactionManager)
                .build();
    }

    private Step secondStep() {
        return new StepBuilder("Second Step", jobRepository)
                .tasklet(tasklet2(), platformTransactionManager)
                .build();
    }

    private Tasklet tasklet1() {
        return (contribution, chunkContext) -> {
            log.info("This is first tasklet step");
            return RepeatStatus.FINISHED;
        };
    }

    private Tasklet tasklet2() {
        return (contribution, chunkContext) -> {
            log.info("This is second tasklet step");
            return RepeatStatus.FINISHED;
        };
    }


}
