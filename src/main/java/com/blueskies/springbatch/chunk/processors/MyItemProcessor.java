package com.blueskies.springbatch.chunk.processors;

import com.blueskies.springbatch.model.StudentJdbc;
import com.blueskies.springbatch.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MyItemProcessor implements ItemProcessor<StudentJdbc, StudentJson> {

    @Override
    public StudentJson process(StudentJdbc item) throws Exception {
        return new StudentJson(
                item.getId(),
                item.getFirstName(),
                item.getLastName(),
                item.getEmail()
        );
    }
}
