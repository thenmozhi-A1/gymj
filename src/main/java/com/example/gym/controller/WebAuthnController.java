package com.example.gym.controller;

import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import com.example.gym.service.WebAuthnService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webauthn")
public class WebAuthnController {

    private final WebAuthnService webAuthnService;
    private final UserRepository userRepository;

    public WebAuthnController(WebAuthnService webAuthnService, UserRepository userRepository) {
        this.webAuthnService = webAuthnService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register/begin")
    public ResponseEntity<?> beginRegistration(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }
            User authUser = (User) auth.getPrincipal();

            String optionsJson = webAuthnService.startRegistration(authUser.getId(), authUser.getEmail(), session);
            // Return raw JSON string representing the options
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(optionsJson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/complete")
    public ResponseEntity<?> completeRegistration(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }
            User authUser = (User) auth.getPrincipal();

            // The frontend passes 'credential' as a JSON object, so we convert it back to a JSON string for the Yubico parser
            String credentialJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.get("credential"));
            
            Map<String, Object> result = webAuthnService.finishRegistration(authUser.getId(), credentialJson, session);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/begin")
    public ResponseEntity<?> beginAuthentication(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String email = (String) request.get("email");
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            User user = userOpt.get();

            String optionsJson = webAuthnService.startAuthentication(user.getId(), session);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(optionsJson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/complete")
    public ResponseEntity<?> completeAuthentication(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String email = (String) request.get("email");
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            User user = userOpt.get();

            String assertionJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.get("assertion"));
            Map<String, Object> result = webAuthnService.finishAuthentication(user.getId(), assertionJson, session);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
