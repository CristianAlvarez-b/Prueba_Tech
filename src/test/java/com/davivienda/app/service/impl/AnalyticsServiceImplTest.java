package com.davivienda.app.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceImplTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Tuple> query;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCategorySummary_withResults() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get("category", String.class)).thenReturn("Comida");
        when(tuple.get("total", BigDecimal.class)).thenReturn(BigDecimal.valueOf(150));

        when(em.createQuery(anyString(), eq(Tuple.class))).thenReturn(query);
        when(query.setParameter(eq("userId"), anyLong())).thenReturn(query);
        when(query.setParameter(eq("from"), any(LocalDate.class))).thenReturn(query);
        when(query.setParameter(eq("to"), any(LocalDate.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(tuple));

        Map<String, Object> result = analyticsService.categorySummary(1L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,31));

        assertNotNull(result);
        assertEquals(LocalDate.of(2025,1,1), result.get("from"));
        assertEquals(LocalDate.of(2025,1,31), result.get("to"));
        List<Map<String, Object>> categories = (List<Map<String, Object>>) result.get("categories");
        assertEquals(1, categories.size());
        assertEquals("Comida", categories.get(0).get("category"));
        assertEquals(BigDecimal.valueOf(150), categories.get(0).get("total"));
    }

    @Test
    void testCategorySummary_emptyList() {
        when(em.createQuery(anyString(), eq(Tuple.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        Map<String, Object> result = analyticsService.categorySummary(1L, null, null);

        assertNotNull(result);
        assertTrue(((List<?>)result.get("categories")).isEmpty());
        assertNotNull(result.get("from"));
        assertNotNull(result.get("to"));
    }

    @Test
    void testMonthlyBalance_withResults() {
        Tuple tuple1 = mock(Tuple.class);
        when(tuple1.get("yr")).thenReturn(2025);
        when(tuple1.get("mon")).thenReturn(11);
        when(tuple1.get("income", BigDecimal.class)).thenReturn(BigDecimal.valueOf(200));
        when(tuple1.get("expense", BigDecimal.class)).thenReturn(BigDecimal.valueOf(50));

        Tuple tuple2 = mock(Tuple.class);
        when(tuple2.get("yr")).thenReturn(2025);
        when(tuple2.get("mon")).thenReturn(12);
        when(tuple2.get("income", BigDecimal.class)).thenReturn(BigDecimal.valueOf(100));
        when(tuple2.get("expense", BigDecimal.class)).thenReturn(BigDecimal.valueOf(80));

        when(em.createQuery(anyString(), eq(Tuple.class))).thenReturn(query);
        when(query.setParameter(eq("userId"), anyLong())).thenReturn(query);
        when(query.setParameter(eq("startDate"), any(LocalDate.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(tuple1, tuple2));

        List<Map<String, Object>> result = analyticsService.monthlyBalance(1L, 3);

        assertEquals(3, result.size());
        // El último mes (12) debe tener balance = 20
        Map<String, Object> lastMonth = result.get(2);
        assertEquals(BigDecimal.valueOf(150), lastMonth.get("balance"));
        Map<String, Object> firstMonth = result.get(0);
        assertEquals(BigDecimal.ZERO, firstMonth.get("income"));
        assertEquals(BigDecimal.ZERO, firstMonth.get("expense"));
        assertEquals(BigDecimal.ZERO, firstMonth.get("balance"));
    }

    @Test
    void testMonthlyBalance_emptyList() {
        when(em.createQuery(anyString(), eq(Tuple.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        List<Map<String, Object>> result = analyticsService.monthlyBalance(1L, 2);

        assertEquals(2, result.size());
        for (Map<String, Object> month : result) {
            assertEquals(BigDecimal.ZERO, month.get("income"));
            assertEquals(BigDecimal.ZERO, month.get("expense"));
            assertEquals(BigDecimal.ZERO, month.get("balance"));
        }
    }
}
