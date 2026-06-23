package com.example.gym.controller;

import com.example.gym.entity.RefreshToken;
import com.example.gym.entity.User;
import com.example.gym.repository.RefreshTokenRepository;
import com.example.gym.repository.UserRepository;
import com.example.gym.security.JwtTokenProvider;
import com.example.gym.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import com.example.gym.entity.PasswordResetToken;
import com.example.gym.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtTokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
            }

            // Using existing UserService to handle login (also seamless BCrypt migration handled inside UserService)
            User user = userService.loginUser(email, password);

            String accessToken = tokenProvider.generateAccessToken(user);
            String rawRefreshToken = tokenProvider.generateRefreshToken();
            
            // Store hashed refresh token
            RefreshToken rt = new RefreshToken(
                    user, 
                    hashToken(rawRefreshToken), 
                    LocalDateTime.now().plus(30, ChronoUnit.DAYS), 
                    "Unknown Device"
            );
            refreshTokenRepository.save(rt);

            ResponseCookie jwtCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(15 * 60).build();
                    
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", rawRefreshToken)
                    .httpOnly(true).secure(false).sameSite("Lax").path("/api/auth/refresh").maxAge(30 * 24 * 60 * 60).build();

            Map<String, Object> response = new HashMap<>();
            response.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getFullName(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "mustChangePassword", user.getMustChangePassword() != null ? user.getMustChangePassword() : false
            ));

            response.put("accessToken", accessToken);
            response.put("refreshToken", rawRefreshToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String cookieRefreshToken, @RequestBody(required = false) Map<String, String> requestBody) {
        String rawRefreshToken = cookieRefreshToken != null ? cookieRefreshToken : (requestBody != null ? requestBody.get("refreshToken") : null);
        if (rawRefreshToken == null) return ResponseEntity.badRequest().body(Map.of("error", "Refresh token required"));

        String hash = hashToken(rawRefreshToken);
        Optional<RefreshToken> rtOpt = refreshTokenRepository.findByTokenHash(hash);

        if (rtOpt.isEmpty() || rtOpt.get().isRevoked() || rtOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid, expired, or revoked refresh token"));
        }

        RefreshToken oldToken = rtOpt.get();
        User user = oldToken.getUser();

        // Rotate token (revoke old, issue new)
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRawRefreshToken = tokenProvider.generateRefreshToken();

        RefreshToken newToken = new RefreshToken(
                user,
                hashToken(newRawRefreshToken),
                LocalDateTime.now().plus(30, ChronoUnit.DAYS),
                oldToken.getDeviceInfo()
        );
        refreshTokenRepository.save(newToken);

        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(15 * 60).build();
                
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRawRefreshToken)
                .httpOnly(true).secure(false).sameSite("Lax").path("/api/auth/refresh").maxAge(30 * 24 * 60 * 60).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of(
                    "message", "Token refreshed successfully",
                    "accessToken", newAccessToken,
                    "refreshToken", newRawRefreshToken
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String rawRefreshToken) {
        if (rawRefreshToken != null) {
            String hash = hashToken(rawRefreshToken);
            Optional<RefreshToken> rtOpt = refreshTokenRepository.findByTokenHash(hash);
            rtOpt.ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        }
        
        ResponseCookie clearJwt = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(0).build();
        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/api/auth/refresh").maxAge(0).build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearJwt.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        User virtualUser = (User) auth.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "id", virtualUser.getId(),
                "name", virtualUser.getFullName(),
                "email", virtualUser.getEmail(),
                "role", virtualUser.getRole()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
            }
            User virtualUser = (User) auth.getPrincipal();

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            userService.changePassword(virtualUser.getId(), currentPassword, newPassword, confirmPassword);

            // Revoke all existing refresh tokens for this user
            revokeAllUserTokens(virtualUser.getId());

            return ResponseEntity.ok(Map.of("success", true, "message", "Password changed. Please log in again."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Generate 6 digit OTP
            String otp = String.format("%06d", new java.util.Random().nextInt(999999));
            String hashedOtp = hashToken(otp);

            PasswordResetToken prt = new PasswordResetToken(user, hashedOtp, LocalDateTime.now().plusMinutes(15));
            passwordResetTokenRepository.save(prt);

            // LOGGING THE OTP SINCE NO SMTP SERVER IS PROVIDED
            System.out.println("==========================================");
            System.out.println("PASSWORD RESET OTP FOR " + email + ": " + otp);
            System.out.println("==========================================");
        }

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset code has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");

            if (email == null || otp == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email, OTP, and new password are required"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Invalid OTP or expired.");
            }
            User user = userOpt.get();

            String hashedOtp = hashToken(otp);
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHash(hashedOtp);

            if (tokenOpt.isEmpty() || !tokenOpt.get().getUser().getId().equals(user.getId()) 
                || tokenOpt.get().getUsed() || tokenOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Invalid OTP or expired.");
            }

            PasswordResetToken prt = tokenOpt.get();
            prt.setUsed(true);
            passwordResetTokenRepository.save(prt);

            Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$");
            if (!pattern.matcher(newPassword).matches()) {
                throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character.");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setMustChangePassword(false);
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            revokeAllUserTokens(user.getId());

            return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successfully. Please log in."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    private void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
