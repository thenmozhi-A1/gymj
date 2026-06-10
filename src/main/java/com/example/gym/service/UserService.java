package com.example.gym.service;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import com.example.gym.repository.StaffRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Register a new user (account creation) */
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }
        // Auto-assign ADMIN role for a specific email
        if ("admin@gym.com".equalsIgnoreCase(user.getEmail())) {
            user.setRole("ADMIN");
        }
        
        // Admin user creation logic (no password provided)
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            String tempPassword = UUID.randomUUID().toString().substring(0, 12);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setMustChangePassword(true);
            
            User savedUser = userRepository.save(user);
            // Temporarily set raw password to return to Admin once
            savedUser.setPassword(tempPassword);
            return savedUser;
        }

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    /** Login - check email + password */
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            verifyAndMigratePassword(user, password);
            return user;
        }

        throw new RuntimeException("User not found with email: " + email);
    }

    private void verifyAndMigratePassword(User user, String rawPassword) {
        String dbPassword = user.getPassword();
        LocalDateTime lockedUntil = user.getLockedUntil();
        Integer failedAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;

        if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account locked. Try again later.");
        }

        boolean isMatch = false;

        // Check if it's already a BCrypt hash
        if (dbPassword != null && dbPassword.startsWith("$2a$")) {
            isMatch = passwordEncoder.matches(rawPassword, dbPassword);
        } else {
            // Plaintext fallback (seamless migration)
            if (dbPassword != null && dbPassword.equals(rawPassword)) {
                isMatch = true;
                // Migrate to BCrypt
                String hashed = passwordEncoder.encode(rawPassword);
                user.setPassword(hashed);
            }
        }

        if (!isMatch) {
            failedAttempts++;
            if (failedAttempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
            }
            user.setFailedLoginAttempts(failedAttempts);
            userRepository.save(user);
            throw new RuntimeException("Invalid email or password");
        }

        // On successful login, reset lockout state
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    /** Biometric Login - check fingerprint hash in both Users and Staffs, with fallback email enrollment */
    public User loginBiometric(String email, String fingerprintHash) {
        if (fingerprintHash == null || fingerprintHash.trim().isEmpty()) {
            throw new RuntimeException("Fingerprint hash is required");
        }
        
        // 1. If email is provided, verify directly by email for maximum reliability and uniqueness
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                    throw new RuntimeException("ACCESS DENIED: Membership is not active.");
                }
                
                String dbHash = user.getFingerprintHash();
                if (dbHash == null || dbHash.trim().isEmpty() || dbHash.startsWith("fp_") || dbHash.equalsIgnoreCase(fingerprintHash)) {
                    user.setFingerprintHash(fingerprintHash);
                    user.setFingerprintEnrolled(true);
                    userRepository.save(user);
                    return user;
                } else {
                    throw new RuntimeException("Fingerprint verification failed for " + email);
                }
            }
        }

        // 2. If email is NOT provided, search by fingerprint hash only (List-based query to avoid NonUniqueResultException)
        java.util.List<User> matchedUsers = userRepository.findByFingerprintHash(fingerprintHash);
        if (matchedUsers.size() == 1) {
            User user = matchedUsers.get(0);
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                throw new RuntimeException("ACCESS DENIED: Membership is not active.");
            }
            return user;
        } else if (matchedUsers.size() > 1) {
            throw new RuntimeException("Multiple users matched this scan. Please enter your email to identify yourself.");
        }

        throw new RuntimeException("Fingerprint not recognized. Please register or try again.");
    }
    /** Get all users */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Get user by ID */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /** Get user by email */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New passwords do not match");
        }

        Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$");
        if (!pattern.matcher(newPassword).matches()) {
            throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /** Update user details */
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setFullName(updatedUser.getFullName());
        existing.setPhone(updatedUser.getPhone());
        existing.setAddress(updatedUser.getAddress());
        existing.setGender(updatedUser.getGender());
        existing.setMembershipType(updatedUser.getMembershipType());
        existing.setStatus(updatedUser.getStatus());
        existing.setRole(updatedUser.getRole());
        existing.setFingerprintHash(updatedUser.getFingerprintHash());
        existing.setFingerprintEnrolled(updatedUser.getFingerprintEnrolled());
        return userRepository.save(existing);
    }

    /** Delete user */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
