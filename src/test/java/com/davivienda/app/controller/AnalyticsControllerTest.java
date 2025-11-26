package com.davivienda.app.controller;

import com.davivienda.app.security.UserPrincipal;
import com.davivienda.app.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userPrincipal.getId()).thenReturn(1L); // Mock final method con Mockito-inline
    }

    @Test
    void testCategorySummary_withoutDates() {
        Map<String, Object> mockResult = Map.of(
                "from", LocalDate.of(2025,1,1),
                "to", LocalDate.of(2025,12,31),
                "categories", List.of(
                        Map.of("category", "Comida", "total", BigDecimal.valueOf(200)),
                        Map.of("category", "Transporte", "total", BigDecimal.valueOf(100))
                )
        );

        when(analyticsService.categorySummary(1L, null, null)).thenReturn(mockResult);

        ResponseEntity<?> response = analyticsController.categorySummary(userPrincipal, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockResult, response.getBody());
        verify(analyticsService, times(1)).categorySummary(1L, null, null);
    }

    @Test
    void testCategorySummary_withDates() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        Map<String, Object> mockResult = Map.of(
                "from", from,
                "to", to,
                "categories", List.of(
                        Map.of("category", "Salud", "total", BigDecimal.valueOf(300))
                )
        );

        when(analyticsService.categorySummary(1L, from, to)).thenReturn(mockResult);

        ResponseEntity<?> response = analyticsController.categorySummary(
                userPrincipal,
                from.toString(),
                to.toString()
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockResult, response.getBody());
        verify(analyticsService, times(1)).categorySummary(1L, from, to);
    }

    @Test
    void testMonthlyBalance_defaultMonths() {
        List<Map<String, Object>> mockResult = List.of(
                Map.of("yearMonth","2025-01","income",BigDecimal.valueOf(1000),"expense",BigDecimal.valueOf(500),"balance",BigDecimal.valueOf(500))
        );

        when(analyticsService.monthlyBalance(1L, 12)).thenReturn(mockResult);

        ResponseEntity<?> response = analyticsController.monthlyBalance(userPrincipal, 12);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockResult, response.getBody());
        verify(analyticsService, times(1)).monthlyBalance(1L, 12);
    }

}
