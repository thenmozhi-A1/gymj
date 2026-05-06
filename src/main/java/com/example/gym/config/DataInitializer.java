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
            // Check if admin already exists
            if (userRepository.findAll().stream().noneMatch(u -> "admin@gym.com".equals(u.getEmail()))) {
                User admin = new User();
                admin.setFullName("admin");
                admin.setEmail("admin@gym.com");
                admin.setPassword("admin");
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");
                userRepository.save(admin);
                System.out.println("✅ Admin account created: admin@gym.com / admin");
            }
        };
    }
}
