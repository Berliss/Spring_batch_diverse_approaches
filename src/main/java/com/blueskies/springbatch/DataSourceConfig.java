package com.blueskies.springbatch;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {


    //Forma de hacer con el builder - esto no funciona quizas en otra version de spring
/*    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder
                .create()
                .url("jdbc:postgresql://localhost:5430/spring_batch")
                .username("berlis")
                .password("123")
                .driverClassName("org.postgresql.Driver")
                .build();
    }*/


    //Con esta manera puedo mapear los valores desde el properties
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties regularDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.configuration")
    public HikariDataSource regularDataSource() {
        HikariDataSource dataSource = regularDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        return dataSource;
    }

}
