package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.dto.AuthRequest;
import com.airtribe.TaskMaster.dto.AuthResponse;
import com.airtribe.TaskMaster.dto.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(AuthRequest request);
}
