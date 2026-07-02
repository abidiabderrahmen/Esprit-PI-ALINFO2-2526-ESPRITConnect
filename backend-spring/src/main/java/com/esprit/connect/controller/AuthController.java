package com.esprit.connect.controller;

import com.esprit.connect.config.JwtUtil;
import com.esprit.connect.dto.LoginRequest;
import com.esprit.connect.dto.RegisterRequest;
import com.esprit.connect.dto.TokenResponse;
import com.esprit.connect.dto.UserDTO;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/token/")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Invalid credentials."));
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Account is inactive."));
        }

        String access = jwtUtil.generateAccessToken(user.getUsername());
        String refresh = jwtUtil.generateRefreshToken(user.getUsername());
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }

    @PostMapping("/auth/token/refresh/")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "Refresh token is required."));
        }

        try {
            String username = jwtUtil.extractUsername(refreshToken);
            if (jwtUtil.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("detail", "Refresh token has expired."));
            }
            String newAccess = jwtUtil.generateAccessToken(username);
            String newRefresh = jwtUtil.generateRefreshToken(username);
            return ResponseEntity.ok(new TokenResponse(newAccess, newRefresh));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Invalid refresh token."));
        }
    }

    @PostMapping({"/auth/register/", "/users/register/"})
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("username", "Username already exists."));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("email", "Email already exists."));
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(Map.of("password_confirm", "Passwords do not match."));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getRole() != null) {
            try {
                user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(User.Role.STUDENT);
            }
        }

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.fromEntity(saved));
    }

    @GetMapping("/auth/profile/")
    public ResponseEntity<?> getProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/users/me/")
    public ResponseEntity<?> getCurrentUserProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @RequestMapping(value = "/users/me/", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody Map<String, Object> updates) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        if (updates.containsKey("first_name")) user.setFirstName((String) updates.get("first_name"));
        if (updates.containsKey("last_name")) user.setLastName((String) updates.get("last_name"));
        if (updates.containsKey("bio")) user.setBio((String) updates.get("bio"));
        if (updates.containsKey("phone")) user.setPhone((String) updates.get("phone"));
        if (updates.containsKey("linkedin_url")) user.setLinkedinUrl((String) updates.get("linkedin_url"));
        if (updates.containsKey("github_url")) user.setGithubUrl((String) updates.get("github_url"));
        if (updates.containsKey("graduation_year")) {
            Object gy = updates.get("graduation_year");
            user.setGraduationYear(gy != null ? Integer.valueOf(gy.toString()) : null);
        }
        if (updates.containsKey("field_of_study")) user.setFieldOfStudy((String) updates.get("field_of_study"));
        if (updates.containsKey("current_position")) user.setCurrentPosition((String) updates.get("current_position"));
        if (updates.containsKey("company_name")) user.setCompanyName((String) updates.get("company_name"));
        if (updates.containsKey("location")) user.setLocation((String) updates.get("location"));
        if (updates.containsKey("skills")) user.setSkills((String) updates.get("skills"));
        if (updates.containsKey("is_mentor")) user.setIsMentor((Boolean) updates.get("is_mentor"));
        if (updates.containsKey("avatar")) user.setAvatar((String) updates.get("avatar"));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserDTO.fromEntity(saved));
    }

    @PostMapping("/auth/forgot-password/")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("email", "Email is required."));
        }
        // In production, send a password reset email here
        return ResponseEntity.ok(Map.of("detail", "Password reset email sent if account exists."));
    }

    @PostMapping("/auth/reset-password/")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        String token = request.get("token");
        String password = request.get("password");
        String passwordConfirm = request.get("password_confirm");

        if (uid == null || token == null || password == null || passwordConfirm == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "All fields are required."));
        }
        if (!password.equals(passwordConfirm)) {
            return ResponseEntity.badRequest().body(Map.of("password_confirm", "Passwords do not match."));
        }

        // In production, validate the reset token and UID
        try {
            Long userId = Long.parseLong(uid);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("detail", "Invalid reset link."));
            }
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("detail", "Password has been reset successfully."));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("detail", "Invalid reset link."));
        }
    }

    @PostMapping("/auth/change-password/")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        String oldPassword = request.get("old_password");
        String newPassword = request.get("new_password");
        String newPasswordConfirm = request.get("new_password_confirm");

        if (oldPassword == null || newPassword == null || newPasswordConfirm == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "All fields are required."));
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("old_password", "Incorrect current password."));
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            return ResponseEntity.badRequest().body(Map.of("new_password_confirm", "Passwords do not match."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("detail", "Password changed successfully."));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String username;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            username = auth.getPrincipal().toString();
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
