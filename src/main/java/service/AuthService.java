package com.netpilot.service;

import com.netpilot.dto.LoginRequest;
import com.netpilot.dto.LoginResponse;
import com.netpilot.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}