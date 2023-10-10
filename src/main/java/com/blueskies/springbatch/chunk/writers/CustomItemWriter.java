package com.blueskies.springbatch.chunk.writers;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CustomItemWriter <T> implements ItemWriter<T> {

    public String typeOfWriter = "";

    public CustomItemWriter() {
        this("");
    }

    public CustomItemWriter(String typeOfWriter) {
        this.typeOfWriter = typeOfWriter;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        System.out.println("***INSIDE "+typeOfWriter+" ITEM WRITER***");
        System.out.println(chunk);
    }
}
