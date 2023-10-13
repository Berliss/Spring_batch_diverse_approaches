package com.blueskies.springbatch.chunk.processors;

import com.blueskies.springbatch.model.StudentCsv;
import com.blueskies.springbatch.model.StudentJdbc;
import com.blueskies.springbatch.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MyItemProcessorForFailures implements ItemProcessor<StudentCsv, StudentJson> {

    @Override
    public StudentJson process(StudentCsv item) throws Exception {

        if (item.getId() == 1) {
            System.out.println("INSIDE PROCESOR");
            throw new NullPointerException();
        }

        return new StudentJson(
                item.getId(),
                item.getFirstName(),
                item.getLastName(),
                item.getEmail()
        );
    }
}
