package com.example.todo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("prod")
public class DatabaseInitializer {

    @Bean
    CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            // Create table if not exists
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "  id BIGSERIAL NOT NULL PRIMARY KEY," +
                "  summary VARCHAR(256) NOT NULL," +
                "  description TEXT," +
                "  status VARCHAR(256) NOT NULL" +
                ")"
            );

            // Insert initial data if table is empty
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tasks", Integer.class);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                    "INSERT INTO tasks (summary, description, status) VALUES (?, ?, ?)",
                    "Spring boot を学ぶ", "TODO アプリを作る", "DONE"
                );
                jdbcTemplate.update(
                    "INSERT INTO tasks (summary, description, status) VALUES (?, ?, ?)",
                    "Spring Security を学ぶ", "ログイン機能を作る", "TODO"
                );
            }
        };
    }
}
