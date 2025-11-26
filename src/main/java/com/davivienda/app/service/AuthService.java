package com.davivienda.app.service;


import com.davivienda.app.dto.auth.JwtResponse;
import com.davivienda.app.dto.auth.LoginRequest;
import com.davivienda.app.dto.auth.RegisterRequest;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
