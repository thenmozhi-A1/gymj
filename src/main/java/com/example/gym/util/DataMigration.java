package com.example.gym.util;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

@Component
public class DataMigration implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataMigration(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        boolean migratedAny = false;

        try (PrintWriter writer = new PrintWriter(new FileWriter("migration_passwords.log", true))) {
            for (User user : users) {
                String currentPassword = user.getPassword();
                
                // If password does not start with BCrypt prefix $2a$ or $2b$ or $2y$, it's plaintext
                if (currentPassword != null && !currentPassword.startsWith("$2")) {
                    String tempPassword = UUID.randomUUID().toString().substring(0, 12);
                    user.setPassword(passwordEncoder.encode(tempPassword));
                    user.setMustChangePassword(true);
                    userRepository.save(user);

                    writer.println("MIGRATED: User Email: " + user.getEmail() + " | Temp Password: " + tempPassword);
                    migratedAny = true;
                }
            }
            if (migratedAny) {
                System.out.println("WARNING: Legacy plaintext passwords were migrated. Check migration_passwords.log for temporary passwords and notify affected users.");
            }
        } catch (IOException e) {
            System.err.println("Failed to write migration log: " + e.getMessage());
        }
    }
}
