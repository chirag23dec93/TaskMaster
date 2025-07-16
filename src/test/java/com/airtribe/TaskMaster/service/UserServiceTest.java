package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Role;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    private User user;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        rawPassword = "password";
        user = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .roles(Collections.singleton(Role.USER))
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = service.register(user.getUsername(), user.getEmail(), rawPassword);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.getRoles(), result.getRoles());
    }

    @Test
    void register_UsernameExists() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.register(user.getUsername(), user.getEmail(), rawPassword));
    }

    @Test
    void register_EmailExists() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.register(user.getUsername(), user.getEmail(), rawPassword));
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User result = (User) service.loadUserByUsername(user.getUsername());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("nonexistent"));
    }

    @Test
    void updateProfile_Success() {
        String newEmail = "newemail@example.com";
        String newPassword = "newpassword";
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = service.updateProfile(user.getUsername(), newEmail, newPassword);

        assertNotNull(result);
        assertEquals(newEmail, result.getEmail());
        assertEquals("newHashedPassword", result.getPassword());
    }

    @Test
    void findByUsername_Success() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        Optional<User> result = service.findByUsername(user.getUsername());

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
        assertEquals(user.getUsername(), result.get().getUsername());
    }

    @Test
    void findById_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<User> result = service.findById(user.getId());

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
        assertEquals(user.getUsername(), result.get().getUsername());
    }
}
