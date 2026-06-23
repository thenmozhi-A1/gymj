package com.example.gym.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseFixRunner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running Database schema fix script to cleanup old employee_id foreign keys...");
        try {
            jdbcTemplate.execute("ALTER TABLE leave_requests DROP FOREIGN KEY FKrxff2xg1kffbjfh5maxwoqyhw");
            logger.info("Dropped foreign key FKrxff2xg1kffbjfh5maxwoqyhw from leave_requests");
        } catch (Exception e) {
            logger.info("Foreign key might already be dropped or doesn't exist: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE leave_requests DROP COLUMN employee_id");
            logger.info("Dropped orphaned column employee_id from leave_requests");
        } catch (Exception e) {
            logger.info("Column employee_id might already be dropped or doesn't exist: " + e.getMessage());
        }
    }
}
