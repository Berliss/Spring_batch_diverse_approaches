package com.blueskies.springbatch.chunk;

import com.blueskies.springbatch.chunk.processors.MyItemProcessor;
import com.blueskies.springbatch.chunk.processors.MyItemProcessorForFailures;
import com.blueskies.springbatch.chunk.skiplisteners.SkipListener;
import com.blueskies.springbatch.chunk.skiplisteners.SkipListenerImpl;
import com.blueskies.springbatch.chunk.writers.*;
import com.blueskies.springbatch.model.StudentCsv;
import com.blueskies.springbatch.model.StudentJdbc;
import com.blueskies.springbatch.model.StudentJson;
import com.blueskies.springbatch.model.StudentXml;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;


@Configuration
@AllArgsConstructor
public class SampleChunkJob {

    private DataSource dataSource;
    private PlatformTransactionManager platformTransactionManager;
    private JobRepository jobRepository;
    private SkipListener skipListener;
    private SkipListenerImpl skipListenerImpl;

    @Bean
    public Job mySecondJob() {
        return new JobBuilder("Chunk example job", jobRepository)
                .incrementer(new MyCustomIdIncrementer())
                .start(firstStepWithCSV())
                .build();
    }

    /* --- Steps --- */

    public Step firstStepWithCSV() {
        return new StepBuilder("first step using flat file", jobRepository)
                .<StudentCsv, StudentCsv>chunk(3, platformTransactionManager)
                .reader(csvFlatFileItemReader(null))
                .writer(new CustomItemWriter<>("CSV"))
                .build();
    }

    public Step firstStepWithJson() {
        return new StepBuilder("first step using JSON file", jobRepository)
                .<StudentJson, StudentJson>chunk(3, platformTransactionManager)
                .reader(jsonItemReader(null))
                .writer(new CustomItemWriter<>("JSON"))
                .build();
    }

    public Step firstStepWithXml() {
        return new StepBuilder("first step using XML file", jobRepository)
                .<StudentXml, StudentXml>chunk(3, platformTransactionManager)
                .reader(xmlStaxEventItemReader(null))
                .writer(new CustomItemWriter<>("XML"))
                .build();
    }

    public Step firstStepWithJdbc() {
        return new StepBuilder("first step using JDBC DB file", jobRepository)
                .<StudentJdbc, StudentJdbc>chunk(3, platformTransactionManager)
                .reader(jdbcItemReader())
                .writer(new CustomItemWriter<>("JDBC"))
                .build();
    }

    public Step firstStepWithJdbcAndFlatFileItemWriter() {
        return new StepBuilder("first step using JDBC DB file and FlatFileItemWriter", jobRepository)
                .<StudentJdbc, StudentJdbc>chunk(3, platformTransactionManager)
                .reader(jdbcItemReader())
                .writer(flatFileItemWriter(null))
                .build();
    }

    public Step firstStepWithJdbcAndJsonItemWriter() {
        return new StepBuilder("first step using JDBC DB file and JsonItemWriter", jobRepository)
                .<StudentJdbc, StudentJson>chunk(3, platformTransactionManager)
                .reader(jdbcItemReader())
                .processor(new MyItemProcessor())
                .writer(jsonFileItemWriter(null))
                .build();
    }

    public Step firstStepWithJdbcAndXmlItemWriter() {
        return new StepBuilder("first step using JDBC DB file and XmlItemWriter", jobRepository)
                .<StudentJdbc, StudentJdbc>chunk(3, platformTransactionManager)
                .reader(jdbcItemReader())
                .writer(xmlFileItemWriter(null))
                .build();
    }

    public Step firstStepWithFlatFileAndJdbcItemWriter() {
        return new StepBuilder("first step using Flat file and JdbcItemWriter", jobRepository)
                .<StudentCsv, StudentCsv>chunk(3, platformTransactionManager)
                .reader(csvFlatFileItemReader(null))
                .writer(jdbcBatchItemWriter())
                .build();
    }

    public Step firstStepWithFlatFileAndJsonItemWriterWithFaultTolerance() {
        return new StepBuilder("first step using Flat file and JdbcItemWriter", jobRepository)
                .<StudentCsv, StudentJson>chunk(3, platformTransactionManager)
                .reader(csvFlatFileItemReaderFaultTolerance(null))
                .processor(new MyItemProcessorForFailures())
                .writer(jsonFileItemWriterToUseWithingFaultTorelanceStep(null))
                .faultTolerant()
                .skip(Throwable.class)
//                .skip(NullPointerException.class)
//                .skipLimit(Integer.MAX_VALUE)
                .skipPolicy(new AlwaysSkipItemSkipPolicy()) // se supone que esta linea junto con retry deberia crear un loop eterno, pero hasta hora no sucedio. Tenlo en cuenta
                .retry(Throwable.class)
                .retryLimit(2)
//                .listener(skipListener)
                .listener(skipListenerImpl)
                .build();
    }


    /* --- READERS --- */

    @Bean
    public JdbcCursorItemReader<StudentJdbc> jdbcItemReader() {
        return new JdbcCursorItemReaderBuilder<StudentJdbc>()
                .dataSource(dataSource)
                .name("Student JDbc Reader")
                .sql("select * from STUDENT")
                .rowMapper((rs, rowNum) -> {
                    StudentJdbc student = new StudentJdbc();
                    student.setId(rs.getLong(1));
                    student.setFirstName(rs.getString(2));
                    student.setLastName(rs.getString(3));
                    student.setEmail(rs.getString(4));
                    return student;
                })
                .build();

    }

    @Bean
    @StepScope
    public JsonItemReader<StudentJson> jsonItemReader(@Value("#{jobParameters['inputFile']}") String fileSystemResource) {
        JsonItemReader<StudentJson> jsonItemReader = new JsonItemReader<>();
//        jsonItemReader.setResource(fileSystemResource);
        jsonItemReader.setResource(new FileSystemResource(fileSystemResource+".json"));
        jsonItemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(StudentJson.class));
        jsonItemReader.setName("JSON item reader");
        jsonItemReader.setMaxItemCount(8);
        jsonItemReader.setCurrentItemCount(2);
        return jsonItemReader;
    }

    @Bean
    @StepScope
    public StaxEventItemReader<StudentXml> xmlStaxEventItemReader(@Value("#{jobParameters['inputFile']}") String fileSystemResource) {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(StudentXml.class);

        StaxEventItemReader<StudentXml> staxEventItemReader = new StaxEventItemReader<>();
//        staxEventItemReader.setResource(fileSystemResource);
        staxEventItemReader.setResource(new FileSystemResource(fileSystemResource+".xml"));
        staxEventItemReader.setFragmentRootElementName("student");
        staxEventItemReader.setUnmarshaller(jaxb2Marshaller);

        return staxEventItemReader;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<StudentCsv> csvFlatFileItemReader(@Value("#{jobParameters['inputFile']}") String fileSystemResource) {
        FlatFileItemReader<StudentCsv> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("csv flatFileItemReader");
        flatFileItemReader.setResource(new FileSystemResource(fileSystemResource+".csv"));
//        flatFileItemReader.setResource(fileSystemResource);
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<StudentCsv> csvFlatFileItemReaderFaultTolerance(@Value("#{jobParameters['inputFileWithError']}") String fileSystemResource) {
        FlatFileItemReader<StudentCsv> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("csv flatFileItemReader Fault Tolerance");
        flatFileItemReader.setResource(new FileSystemResource(fileSystemResource));
//        flatFileItemReader.setResource(fileSystemResource);
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;
    }

    private LineMapper<StudentCsv> lineMapper() {
        DefaultLineMapper<StudentCsv> defaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("id", "firstName", "lastName", "email");
        delimitedLineTokenizer.setStrict(true); // this was set to true to enable failures with delimited files, on first place was set to true

        BeanWrapperFieldSetMapper<StudentCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(StudentCsv.class);

        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        return defaultLineMapper;
    }

    /* --- WRITERS --- */

    @Bean
    @StepScope
    public FlatFileItemWriter<StudentJdbc> flatFileItemWriter(@Value("#{jobParameters['outputFile']}") String fileSystemResource) {

        BeanWrapperFieldExtractor<StudentJdbc> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "firstName", "lastName", "email"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<StudentJdbc> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<StudentJdbc> flatFileItemWriter = new FlatFileItemWriter<>();
//        flatFileItemWriter.setResource(fileSystemResource);
        flatFileItemWriter.setResource(new FileSystemResource(fileSystemResource+".csv"));
        flatFileItemWriter.setLineAggregator(lineAggregator);
        flatFileItemWriter.setHeaderCallback(writer -> writer.write("ID,FIRST NAME,LASTNAME,EMAIL"));
        flatFileItemWriter.setFooterCallback(writer -> writer.write("Created at " + LocalDateTime.now()));

        return flatFileItemWriter;
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<StudentJson> jsonFileItemWriter(@Value("#{jobParameters['outputFile']}") String fileSystemResource) {
        return new JsonFileItemWriter<>(new FileSystemResource(fileSystemResource+".json"),new JacksonJsonObjectMarshaller<>());
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<StudentJson> jsonFileItemWriterToUseWithingFaultTorelanceStep(@Value("#{jobParameters['outputFile']}") String fileSystemResource) {
        return new JsonFileItemWriter<>(new FileSystemResource(fileSystemResource+".json"),new JacksonJsonObjectMarshaller<>()){
            @Override
            public String doWrite(Chunk<? extends StudentJson> items) {
                items.forEach(studentJson -> {
                    if (studentJson.getId() == 10) {
                        System.out.println("INSIDE WRITER");
                        throw new NullPointerException();
                    }
                });
                return super.doWrite(items);
            }
        };
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<StudentJdbc> xmlFileItemWriter(@Value("#{jobParameters['outputFile']}") String fileSystemResource) {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(StudentJdbc.class);

        StaxEventItemWriter<StudentJdbc> staxEventItemWriter = new StaxEventItemWriter<>();
        staxEventItemWriter.setRootTagName("students");
//        staxEventItemWriter.setResource(fileSystemResource);
        staxEventItemWriter.setResource(new FileSystemResource(fileSystemResource+".xml"));
        staxEventItemWriter.setMarshaller(jaxb2Marshaller);

        return staxEventItemWriter;
    }

    @Bean // this @bean annotation is a must-have to make a jdbcWriter work
    @StepScope
    public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setDataSource(dataSource);
        jdbcBatchItemWriter.setSql("insert into student(id, first_name, last_name, email) values(:id, :firstName, :lastName, :email)");
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

/*
        --- PREPARED STATEMENT APPROACH ---

        jdbcBatchItemWriter.setSql("insert into student(id, first_name, last_name, email) values(?,?,?,?)");
        jdbcBatchItemWriter.setItemPreparedStatementSetter((item, ps) -> {
            ps.setLong(1, item.getId());
            ps.setString(2, item.getFirstName());
            ps.setString(3, item.getLastName());
            ps.setString(4, item.getEmail());
        });
*/

        return jdbcBatchItemWriter;
    }
}
