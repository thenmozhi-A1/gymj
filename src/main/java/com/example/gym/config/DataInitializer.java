package com.example.gym.config;

import com.example.gym.entity.Attendance;
import com.example.gym.entity.Payment;
import com.example.gym.entity.User;
import com.example.gym.repository.AttendanceRepository;
import com.example.gym.repository.PaymentRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, AttendanceRepository attendanceRepository, PaymentRepository paymentRepository, JdbcTemplate jdbcTemplate) {
        return args -> {
            // Guarantee 'staffs' table exists in database
            try {
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS staffs (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "full_name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "phone VARCHAR(255), " +
                    "address VARCHAR(255), " +
                    "role VARCHAR(255), " +
                    "salary VARCHAR(255), " +
                    "times VARCHAR(255), " +
                    "specialty VARCHAR(255), " +
                    "leaves INT DEFAULT 0, " +
                    "permissions INT DEFAULT 0, " +
                    "fingerprint_hash VARCHAR(255), " +
                    "fingerprint_enrolled BOOLEAN DEFAULT FALSE, " +
                    "status VARCHAR(255) DEFAULT 'ACTIVE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB"
                );
                System.out.println("✅ Database table 'staffs' checked/created successfully.");
            } catch (Exception e) {
                System.err.println("❌ Failed to verify/create 'staffs' table: " + e.getMessage());
            }

            String adminEmail = "admin@gym.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setFullName("Gym Admin");
                admin.setEmail(adminEmail);
                admin.setPassword("admin");
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");
                userRepository.save(admin);
                System.out.println("✅ Default Admin account created: " + adminEmail + " / admin");
            } else {
                System.out.println("ℹ️ Admin account already exists.");
            }

            // Cleanup existing sample data if present in DB
            try {
                jdbcTemplate.execute("DELETE FROM attendance WHERE user_id IN (SELECT id FROM users WHERE email IN ('john.doe@example.com', 'jane.smith@example.com', 'mike.johnson@example.com', 'sarah.williams@example.com', 'david.brown@example.com', 'emily.davis@example.com'))");
                jdbcTemplate.execute("DELETE FROM payment WHERE user_id IN (SELECT id FROM users WHERE email IN ('john.doe@example.com', 'jane.smith@example.com', 'mike.johnson@example.com', 'sarah.williams@example.com', 'david.brown@example.com', 'emily.davis@example.com'))");
                jdbcTemplate.execute("DELETE FROM users WHERE email IN ('john.doe@example.com', 'jane.smith@example.com', 'mike.johnson@example.com', 'sarah.williams@example.com', 'david.brown@example.com', 'emily.davis@example.com')");
                System.out.println("✅ Cleaned up any old sample data from the database.");
            } catch (Exception e) {
                System.out.println("ℹ️ No sample data to clean up, or error during cleanup.");
            }
        };
    }
}
