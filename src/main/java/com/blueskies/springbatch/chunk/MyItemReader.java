package com.blueskies.springbatch.chunk;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class MyItemReader implements ItemReader<Integer> {

    private List<Integer> source = List.of(1,2,3,4,5,6,7,8,9,10);
    private int i =  0;
    @Override
    public Integer read() throws Exception {
        Integer value;
        if (i < source.size()) {
            value = source.get(i);
            System.out.println("Value read -> "+ value);
            i++;
            return value;
        }
        return null;
    }
}
