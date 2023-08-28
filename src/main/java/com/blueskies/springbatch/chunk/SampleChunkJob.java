package com.blueskies.springbatch.chunk;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@AllArgsConstructor
public class SampleChunkJob {

    private MyItemReader itemReader;
    private MyItemWriter itemWriter;
    private MyItemProcessor itemProcessor;

    private PlatformTransactionManager platformTransactionManager;
    private JobRepository jobRepository;

    @Bean
    public Job mySecondJob() {
        return new JobBuilder("Chunk example job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstStep())
                .next(secondStep())
                .build();
    }

    public Step firstStep() {
        return new StepBuilder("first step", jobRepository)
                .<Integer, Long>chunk(3, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    public Step secondStep() {
        return new StepBuilder("second step", jobRepository)
                .tasklet(tasklet(),platformTransactionManager)
                .build();
    }

    public Tasklet tasklet() {
        return (contribution, chunkContext) -> {
            System.out.println("*** ACCION DESDE TASKLET ***");
            return RepeatStatus.FINISHED;
        };
    }
}
