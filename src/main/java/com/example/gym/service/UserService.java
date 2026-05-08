package com.example.gym.service;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
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
        return userRepository.save(existing);
    }

    /** Delete user */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
