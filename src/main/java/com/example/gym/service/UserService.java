package com.example.gym.service;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import com.example.gym.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public UserService(UserRepository userRepository, StaffRepository staffRepository) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
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
        return userRepository.save(user);
    }

    /** Login - check email + password */
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("Invalid password");
            }
            return user;
        }

        // If not found in User, check Staff table!
        Optional<com.example.gym.entity.Staff> staffOpt = staffRepository.findByEmail(email);
        if (staffOpt.isPresent()) {
            com.example.gym.entity.Staff staff = staffOpt.get();
            if (!staff.getPassword().equals(password)) {
                throw new RuntimeException("Invalid password");
            }
            // Map Staff to a virtual User object for authentication flow compatibility
            User user = new User();
            user.setId(staff.getId());
            user.setFullName(staff.getFullName());
            user.setEmail(staff.getEmail());
            user.setPassword(staff.getPassword());
            user.setPhone(staff.getPhone());
            user.setAddress(staff.getAddress());
            user.setRole(staff.getRole());
            user.setSalary(staff.getSalary());
            user.setTimes(staff.getTimes());
            user.setSpecialty(staff.getSpecialty());
            user.setLeaves(staff.getLeaves());
            user.setPermissions(staff.getPermissions());
            user.setFingerprintHash(staff.getFingerprintHash());
            user.setFingerprintEnrolled(staff.getFingerprintEnrolled());
            user.setStatus(staff.getStatus());
            return user;
        }

        throw new RuntimeException("User not found with email: " + email);
    }

    /** Biometric Login - check fingerprint hash in both Users and Staffs, with fallback email enrollment */
    public User loginBiometric(String email, String fingerprintHash) {
        if (fingerprintHash == null || fingerprintHash.trim().isEmpty()) {
            throw new RuntimeException("Fingerprint hash is required");
        }
        
        // 1. Search in standard members table (User) by hash
        Optional<User> userOpt = userRepository.findByFingerprintHash(fingerprintHash);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                throw new RuntimeException("ACCESS DENIED: Membership is not active.");
            }
            return user;
        }

        // 2. Search in separate staffs table (Staff) by hash
        Optional<com.example.gym.entity.Staff> staffOpt = staffRepository.findByFingerprintHash(fingerprintHash);
        if (staffOpt.isPresent()) {
            com.example.gym.entity.Staff staff = staffOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(staff.getStatus())) {
                throw new RuntimeException("ACCESS DENIED: Employee status is not active.");
            }
            return mapStaffToUser(staff);
        }

        // 3. Fallback: Search by email if provided to automatically activate new devices/fingerprints
        if (email != null && !email.trim().isEmpty()) {
            // Check Staff first
            Optional<com.example.gym.entity.Staff> staffByEmail = staffRepository.findByEmail(email);
            if (staffByEmail.isPresent()) {
                com.example.gym.entity.Staff staff = staffByEmail.get();
                if (!"ACTIVE".equalsIgnoreCase(staff.getStatus())) {
                    throw new RuntimeException("ACCESS DENIED: Employee status is not active.");
                }
                
                // Update and save new fingerprint hash in staffs table
                staff.setFingerprintHash(fingerprintHash);
                staff.setFingerprintEnrolled(true);
                staffRepository.save(staff);
                
                // Also update matching user record if exists
                Optional<User> userByEmail = userRepository.findByEmail(email);
                if (userByEmail.isPresent()) {
                    User u = userByEmail.get();
                    u.setFingerprintHash(fingerprintHash);
                    u.setFingerprintEnrolled(true);
                    userRepository.save(u);
                }
                
                return mapStaffToUser(staff);
            }

            // Check User standard members
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                User user = userByEmail.get();
                if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                    throw new RuntimeException("ACCESS DENIED: Membership is not active.");
                }
                
                user.setFingerprintHash(fingerprintHash);
                user.setFingerprintEnrolled(true);
                userRepository.save(user);
                return user;
            }
        }

        throw new RuntimeException("Fingerprint not recognized. Please register or try again.");
    }

    private User mapStaffToUser(com.example.gym.entity.Staff staff) {
        User user = new User();
        user.setId(staff.getId());
        user.setFullName(staff.getFullName());
        user.setEmail(staff.getEmail());
        user.setPassword(staff.getPassword());
        user.setPhone(staff.getPhone());
        user.setAddress(staff.getAddress());
        user.setRole(staff.getRole());
        user.setSalary(staff.getSalary());
        user.setTimes(staff.getTimes());
        user.setSpecialty(staff.getSpecialty());
        user.setLeaves(staff.getLeaves());
        user.setPermissions(staff.getPermissions());
        user.setFingerprintHash(staff.getFingerprintHash());
        user.setFingerprintEnrolled(staff.getFingerprintEnrolled());
        user.setStatus(staff.getStatus());
        return user;
    }


    /** Get all users */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Get user by ID */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
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
        existing.setSalary(updatedUser.getSalary());
        existing.setTimes(updatedUser.getTimes());
        existing.setSpecialty(updatedUser.getSpecialty());
        existing.setLeaves(updatedUser.getLeaves());
        existing.setPermissions(updatedUser.getPermissions());
        existing.setFingerprintHash(updatedUser.getFingerprintHash());
        existing.setFingerprintEnrolled(updatedUser.getFingerprintEnrolled());
        return userRepository.save(existing);
    }

    /** Delete user */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
