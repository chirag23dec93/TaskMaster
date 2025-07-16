package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.dto.AuthRequest;
import com.airtribe.TaskMaster.dto.AuthResponse;
import com.airtribe.TaskMaster.dto.SignupRequest;
import com.airtribe.TaskMaster.entity.Role;
import com.airtribe.TaskMaster.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthService service;

    private User user;
    private SignupRequest signupRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .roles(Collections.singleton(Role.USER))
                .build();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password");

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password");


    }

    @Test
    void signup_Success() {
        AuthResponse expectedResponse = AuthResponse.builder()
                .token("jwt-token")
                .username(user.getUsername())
                .build();
        when(service.signup(signupRequest)).thenReturn(expectedResponse);

        AuthResponse response = service.signup(signupRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    void signup_UsernameExists() {
        when(service.signup(signupRequest)).thenThrow(new IllegalArgumentException("Username already taken"));

        assertThrows(IllegalArgumentException.class, () -> service.signup(signupRequest));
    }

    @Test
    void signup_EmailExists() {
        when(service.signup(signupRequest)).thenThrow(new IllegalArgumentException("Email already taken"));

        assertThrows(IllegalArgumentException.class, () -> service.signup(signupRequest));
    }

    @Test
    void login_Success() {
        AuthResponse expectedResponse = AuthResponse.builder()
                .token("jwt-token")
                .username(user.getUsername())
                .build();
        when(service.login(authRequest)).thenReturn(expectedResponse);

        AuthResponse response = service.login(authRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    void login_InvalidCredentials() {
        when(service.login(authRequest)).thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> service.login(authRequest));
    }

    @Test
    void login_UserNotFound() {
        when(service.login(authRequest)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> service.login(authRequest));
    }
}
