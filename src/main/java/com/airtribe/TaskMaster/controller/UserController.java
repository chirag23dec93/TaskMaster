package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.dto.UpdateProfileRequest;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication auth) {
        // Get existing user from database by username
        User user = userService.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest req) {
        User updated = userService.updateProfile(
                userDetails.getUsername(),
                req.getEmail(),
                req.getPassword()
        );
        return ResponseEntity.ok(updated);
    }
}
