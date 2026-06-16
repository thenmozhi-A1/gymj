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

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, AttendanceRepository attendanceRepository, PaymentRepository paymentRepository, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
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
            User admin = userRepository.findByEmail(adminEmail).orElse(new User());
            admin.setFullName("Gym Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            userRepository.save(admin);
            System.out.println("✅ Default Admin account created/updated: " + adminEmail + " / admin");

            // Cleanup existing sample data (if any from previous deployments)
            String[] sampleUsers = {
                "john.doe@example.com", "jane.smith@example.com", "mike.johnson@example.com",
                "sarah.williams@example.com", "david.brown@example.com", "emily.davis@example.com"
            };
            
            for (String email : sampleUsers) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    // Delete attendances
                    attendanceRepository.findByUserId(user.getId()).forEach(att -> attendanceRepository.delete(att));
                    // Delete payments
                    paymentRepository.findByUserId(user.getId()).forEach(pay -> paymentRepository.delete(pay));
                    // Delete user
                    userRepository.delete(user);
                    System.out.println("🗑️ Deleted sample user and associated data: " + email);
                });
            }
        };
    }
}
