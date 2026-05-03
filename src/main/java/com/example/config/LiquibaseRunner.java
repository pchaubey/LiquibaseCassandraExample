package com.example.config;

import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@Component
@Profile({"local-liquibase"})
@RequiredArgsConstructor
public class LiquibaseRunner {

    private final Environment environment;

    @PostConstruct
    public void liquibaseRunner() {
        try {
            //Ensure driver is on classpath
            Class.forName("com.ing.data.cassandra.jdbc.CassandraDriver");
            var url = environment.getRequiredProperty("spring.liquibase.url");
            var username = environment.getRequiredProperty("spring.liquibase.username");
            var password = environment.getRequiredProperty("spring.liquibase.password");

            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("sslEngineFactory", LiquibaseSSLEngineFactory.class.getCanonicalName());

            try (Connection conn = DriverManager.getConnection(url, properties)) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(conn));

                try (Liquibase liquibase = new Liquibase(
                        "changelog.xml",
                        new ClassLoaderResourceAccessor(),
                        database)) {

                    liquibase.update(); // run all pending changes
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAbsolutePath(String path) {
        String resourcePath = path.startsWith("classpath:") ? path.substring("classpath:".length()) : path;
        var resource = this.getClass().getClassLoader().getResource(resourcePath);
        try {
            return new File(resource.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            return path;
        }
    }

}
