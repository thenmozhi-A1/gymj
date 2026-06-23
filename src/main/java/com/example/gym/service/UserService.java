package com.example.gym.service;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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


    /** Get all users */
    public List<User> getAllUsers() {
        return userRepository.findByRoleIn(java.util.Collections.singletonList("USER"));
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
        
        // Add missing fields
        existing.setDob(updatedUser.getDob());
        existing.setAge(updatedUser.getAge());
        existing.setCity(updatedUser.getCity());
        existing.setHeight(updatedUser.getHeight());
        existing.setWeight(updatedUser.getWeight());
        existing.setBmi(updatedUser.getBmi());
        existing.setBloodGroup(updatedUser.getBloodGroup());
        existing.setFitnessGoal(updatedUser.getFitnessGoal());
        existing.setMembershipPlan(updatedUser.getMembershipPlan());
        existing.setStartDate(updatedUser.getStartDate());
        existing.setExpiryDate(updatedUser.getExpiryDate());
        existing.setReferralSource(updatedUser.getReferralSource());
        existing.setEmergencyContactName(updatedUser.getEmergencyContactName());
        existing.setEmergencyContactNumber(updatedUser.getEmergencyContactNumber());
        existing.setMedicalConditions(updatedUser.getMedicalConditions());
        existing.setAllergies(updatedUser.getAllergies());

        boolean statusOrRoleChanged = !existing.getStatus().equals(updatedUser.getStatus()) || !existing.getRole().equals(updatedUser.getRole());
        
        existing.setStatus(updatedUser.getStatus());
        existing.setRole(updatedUser.getRole());
        
        if (statusOrRoleChanged) {
            Long currentVersion = existing.getTokenVersion() != null ? existing.getTokenVersion() : 0L;
            existing.setTokenVersion(currentVersion + 1);
        }

        return userRepository.save(existing);
    }

    /** Delete user */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
