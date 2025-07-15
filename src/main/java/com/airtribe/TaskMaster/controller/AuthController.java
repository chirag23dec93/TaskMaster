package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.security.JwtUtil;
import com.airtribe.TaskMaster.service.UserService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req.getUsername(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            // Get the user ID from the database
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            return ResponseEntity.ok(new JwtResponse(token, user.getId()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        // Optionally: userService.logout(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Data static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        public String getUsername() {
            return username;
        }
        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
    @Data static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }
        public String getPassword() {
            return password;
        }
    }
    @Data @AllArgsConstructor 
    static class JwtResponse { 
        private String token;
        private String userId;
    }
}
