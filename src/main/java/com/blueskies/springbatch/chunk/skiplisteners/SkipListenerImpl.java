package com.blueskies.springbatch.chunk.skiplisteners;

import com.blueskies.springbatch.model.StudentCsv;
import com.blueskies.springbatch.model.StudentJson;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class SkipListenerImpl implements SkipListener<StudentCsv, StudentJson> {

    private static final String PATH_SKIP_ON_READ = "src/main/resources/skip_files/reader/" + System.currentTimeMillis() + "_SkipInRead.txt";
    private static final String PATH_SKIP_ON_PROCESS = "src/main/resources/skip_files/process/" + System.currentTimeMillis() + "_SkipInProcess.txt";
    private static final String PATH_SKIP_ON_WRITE = "src/main/resources/skip_files/writer/" + System.currentTimeMillis() + "_SkipInWrite.txt";


    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException) {
            createFile(PATH_SKIP_ON_READ, ((FlatFileParseException) t).getInput());
        }
    }

    @Override
    public void onSkipInProcess(StudentCsv item, Throwable t) {
        if (t instanceof NullPointerException) {
            createFile(PATH_SKIP_ON_PROCESS, item.toString());
        }
    }

    @Override
    public void onSkipInWrite(StudentJson item, Throwable t) {
        if (t instanceof NullPointerException) {
            createFile(PATH_SKIP_ON_WRITE, item.toString());
        }
    }

    private void createFile(String path, String input) {
        try (FileWriter fileWriter = new FileWriter(path, true)) {
            fileWriter.write(input + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
