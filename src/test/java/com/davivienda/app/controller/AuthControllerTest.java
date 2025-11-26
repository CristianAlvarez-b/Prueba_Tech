package com.davivienda.app.controller;

import com.davivienda.app.dto.auth.JwtResponse;
import com.davivienda.app.dto.auth.LoginRequest;
import com.davivienda.app.dto.auth.RegisterRequest;
import com.davivienda.app.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user");
        req.setEmail("user@example.com");
        req.setPassword("password");

        // No necesitamos que devuelva nada porque es void
        doNothing().when(authService).register(req);

        ResponseEntity<?> response = authController.register(req);

        assertEquals(200, response.getStatusCode().value());
        verify(authService, times(1)).register(req);
    }

    @Test
    void testLogin_success() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("user");
        req.setPassword("password");

        JwtResponse mockResp = new JwtResponse("mock-token");
        when(authService.login(req)).thenReturn(mockResp);

        ResponseEntity<JwtResponse> response = authController.login(req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("mock-token", response.getBody().getToken());
        verify(authService, times(1)).login(req);
    }
}
