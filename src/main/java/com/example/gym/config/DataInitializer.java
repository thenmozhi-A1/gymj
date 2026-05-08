package com.example.gym.config;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
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
        };
    }
}
