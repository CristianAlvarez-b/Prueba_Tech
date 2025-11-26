package com.davivienda.app.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidation() {
        // Mock de BindingResult y FieldError
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<?> response = handler.handleValidation(ex);
        assertEquals(400, response.getStatusCode().value());

        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("must not be null", body.get("fieldName"));
    }

    @Test
    void testHandleBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<?> response = handler.handleBadRequest(ex);

        assertEquals(400, response.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Invalid argument", body.get("error"));
    }

    @Test
    void testHandleForbidden() {
        SecurityException ex = new SecurityException("Forbidden action");
        ResponseEntity<?> response = handler.handleForbidden(ex);

        assertEquals(403, response.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Forbidden action", body.get("error"));
    }

    @Test
    void testHandleAll() {
        Exception ex = new Exception("Some error");
        ResponseEntity<?> response = handler.handleAll(ex);

        assertEquals(500, response.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Internal server error", body.get("error"));
    }
}
