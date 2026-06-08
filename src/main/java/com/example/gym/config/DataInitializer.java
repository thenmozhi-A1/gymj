package com.example.gym.config;

import com.example.gym.entity.Attendance;
import com.example.gym.entity.User;
import com.example.gym.repository.AttendanceRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, AttendanceRepository attendanceRepository, JdbcTemplate jdbcTemplate) {
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

            // Create sample users for attendance data
            String[] sampleUsers = {
                "john.doe@example.com", "jane.smith@example.com", "mike.johnson@example.com",
                "sarah.williams@example.com", "david.brown@example.com", "emily.davis@example.com"
            };
            
            String[] sampleNames = {
                "John Doe", "Jane Smith", "Mike Johnson", 
                "Sarah Williams", "David Brown", "Emily Davis"
            };

            for (int i = 0; i < sampleUsers.length; i++) {
                if (!userRepository.existsByEmail(sampleUsers[i])) {
                    User user = new User();
                    user.setFullName(sampleNames[i]);
                    user.setEmail(sampleUsers[i]);
                    user.setPassword("password123");
                    user.setRole("MEMBER");
                    user.setStatus("ACTIVE");
                    user.setMembershipType("Standard");
                    userRepository.save(user);
                    System.out.println("✅ Sample user created: " + sampleNames[i]);
                }
            }

            // Add sample attendance data for the last 7 days
            LocalDate today = LocalDate.now();
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate date = today.minusDays(dayOffset);
                
                for (int i = 0; i < sampleUsers.length; i++) {
                    User user = userRepository.findByEmail(sampleUsers[i]).orElse(null);
                    if (user != null) {
                        // Check if attendance already exists for this user on this date
                        if (attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), date).isEmpty()) {
                            Attendance attendance = new Attendance();
                            attendance.setUser(user);
                            attendance.setAttendanceDate(date);
                            
                            // Random check-in times between 6 AM and 10 AM
                            int hour = 6 + (int)(Math.random() * 4);
                            int minute = (int)(Math.random() * 60);
                            attendance.setCheckInTime(LocalTime.of(hour, minute));
                            
                            // Random check-out times between 8 AM and 12 PM
                            int outHour = 8 + (int)(Math.random() * 4);
                            int outMinute = (int)(Math.random() * 60);
                            attendance.setCheckOutTime(LocalTime.of(outHour, outMinute));
                            
                            attendance.setStatus("PRESENT");
                            attendance.setNotes("Regular workout session");
                            attendanceRepository.save(attendance);
                        }
                    }
                }
            }
            System.out.println("✅ Sample attendance data added for the last 7 days.");
        };
    }
}
