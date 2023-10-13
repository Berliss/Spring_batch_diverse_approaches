package com.blueskies.springbatch.chunk;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.Nullable;

/*
* This class was created to solve the problem that comes with the default implementation
* the default one take the previous jobParameters used on previous job execution
* */
public class MyCustomIdIncrementer implements JobParametersIncrementer {

    private static final String RUN_ID_KEY = "run.id";

    private String key = RUN_ID_KEY;

    /**
     * The name of the run id in the job parameters. Defaults to "run.id".
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Increment the run.id parameter (starting with 1).
     * @param parameters the previous job parameters
     * @return the next job parameters with an incremented (or initialized) run.id
     * @throws IllegalArgumentException if the previous value of run.id is invalid
     */
    @Override
    public JobParameters getNext(@Nullable JobParameters parameters) {

        JobParameters params = (parameters == null) ? new JobParameters() : parameters;
        JobParameter runIdParameter = params.getParameters().get(this.key);
        long id = 1;
        if (runIdParameter != null) {
            try {
                id = Long.parseLong(runIdParameter.getValue().toString()) + 1;
            }
            catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid value for parameter " + this.key, exception);
            }
        }
        return new JobParametersBuilder(new JobParameters()).addLong(this.key, id).toJobParameters();
    }

}