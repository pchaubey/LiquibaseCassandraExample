package com.example.config;

import com.datastax.oss.driver.api.core.CqlSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class CassandraConfiguration {

    @Profile("local-liquibase")
    @Bean(name = "cqlSession")
    @DependsOn("liquibaseRunner")
    public CqlSession cqlSession2() {
        return CqlSession
                .builder()
                .withLocalDatacenter("DC1")
                .withKeyspace("my_keyspace")
                .build();
    }
}