package com.davivienda.app.service.impl;

import com.davivienda.app.dto.auth.JwtResponse;
import com.davivienda.app.dto.auth.LoginRequest;
import com.davivienda.app.dto.auth.RegisterRequest;
import com.davivienda.app.entity.User;
import com.davivienda.app.repository.UserRepository;
import com.davivienda.app.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =================== REGISTER ===================

    @Test
    void testRegister_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setEmail("user1@example.com");
        request.setPassword("pass123");

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("user1", savedUser.getUsername());
        assertEquals("user1@example.com", savedUser.getEmail());
        assertTrue(new BCryptPasswordEncoder().matches("pass123", savedUser.getPassword()));
        assertEquals("ROLE_USER", savedUser.getRoles());
    }

    @Test
    void testRegister_usernameTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setEmail("user1@example.com");
        request.setPassword("pass123");

        when(userRepository.existsByUsername("user1")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("Username already taken", ex.getMessage());
    }

    @Test
    void testRegister_emailTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setEmail("user1@example.com");
        request.setPassword("pass123");

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("Email already in use", ex.getMessage());
    }

    // =================== LOGIN ===================


    @Test
    void testLogin_successByEmail() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("user1@example.com");
        request.setPassword("pass123");

        User user = User.builder().id(2L).username("user1").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // o un mock de Authentication si lo necesitas
        when(userRepository.findByUsername("user1@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user));
        when(jwtProvider.generateToken(2L, "user1")).thenReturn("mock-token-2");

        JwtResponse response = authService.login(request);

        assertEquals("mock-token-2", response.getToken());
    }

    @Test
    void testLogin_userNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("unknown");
        request.setPassword("pass123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // o un mock de Authentication si lo necesitas
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("User not found", ex.getMessage());
    }
}
