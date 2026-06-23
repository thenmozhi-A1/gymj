package com.example.gym.security;

import com.example.gym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TemporarySecurityFix implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TemporarySecurityFix(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String[] affectedEmails = {
            "admin@gym.com", "john.doe@example.com", "jane.smith@example.com",
            "mike.johnson@example.com", "sarah.williams@example.com", "david.brown@example.com",
            "emily.davis@example.com", "testleave1781861107430@gym.com"
        };
        
        for (String email : affectedEmails) {
            userRepository.findByEmail(email).ifPresent(user -> {
                if (email.equals("admin@gym.com")) {
                    user.setPassword(passwordEncoder.encode("Admin@Gym2026!Secure"));
                } else {
                    user.setPassword(passwordEncoder.encode("Temp!Pass123" + System.currentTimeMillis()));
                }
                user.setMustChangePassword(true);
                userRepository.save(user);
                System.out.println("Rotated password for " + email);
            });
        }
    }
}
