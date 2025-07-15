package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Role;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public User register(String username, String email, String rawPassword) {
        if (userRepo.existsByUsername(username) || userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Username or email already taken");
        }
        User u = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(Collections.singleton(Role.USER))
                .build();
        return userRepo.save(u);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @Transactional
    public User updateProfile(String username, String email, String rawPassword) {
        User user = (User) loadUserByUsername(username);
        boolean updated = false;
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            user.setEmail(email);
            updated = true;
        }
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            updated = true;
        }
        return updated ? userRepo.save(user) : user;
    }

    @Transactional
    public void logout(String username) {
        // For JWT stateless logout, you may add the token to blacklist here if implemented.
        // Currently this is a no-op (client should discard token).
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id);
    }
}
