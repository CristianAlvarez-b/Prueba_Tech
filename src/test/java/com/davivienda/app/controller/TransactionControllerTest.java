package com.davivienda.app.controller;

import com.davivienda.app.dto.transaction.TransactionRequest;
import com.davivienda.app.dto.transaction.TransactionResponse;
import com.davivienda.app.security.UserPrincipal;
import com.davivienda.app.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userPrincipal.getId()).thenReturn(1L);
    }

    @Test
    void testCreateTransaction() {
        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.of(2025,1,1));
        req.setAmount(BigDecimal.valueOf(100));
        req.setCategory("Comida");
        req.setType("INCOME");
        req.setDescription("Test Tx");

        TransactionResponse resp = new TransactionResponse(1L, req.getDate(), req.getAmount(), req.getCategory(), req.getType(), req.getDescription());
        when(transactionService.create(1L, req)).thenReturn(resp);

        ResponseEntity<TransactionResponse> response = transactionController.create(userPrincipal, req);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
        verify(transactionService, times(1)).create(1L, req);
    }

    @Test
    void testListTransactions() {
        TransactionResponse tx = new TransactionResponse(1L, LocalDate.now(), BigDecimal.TEN, "Cat", "INCOME", "Desc");
        Page<TransactionResponse> page = new PageImpl<>(List.of(tx));

        when(transactionService.findAll(eq(1L), anyMap(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<TransactionResponse>> response = transactionController.list(userPrincipal, Map.of(), PageRequest.of(0,10));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getTotalElements());
        verify(transactionService, times(1)).findAll(eq(1L), anyMap(), any(Pageable.class));
    }

    @Test
    void testGetTransaction() {
        TransactionResponse resp = new TransactionResponse(1L, LocalDate.now(), BigDecimal.TEN, "Cat", "INCOME", "Desc");
        when(transactionService.getById(1L, 1L)).thenReturn(resp);

        ResponseEntity<TransactionResponse> response = transactionController.get(userPrincipal, 1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
        verify(transactionService, times(1)).getById(1L, 1L);
    }

    @Test
    void testUpdateTransaction() {
        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.now());
        req.setAmount(BigDecimal.valueOf(200));
        req.setCategory("Viaje");
        req.setType("EXPENSE");
        req.setDescription("Actualizado");

        TransactionResponse resp = new TransactionResponse(1L, req.getDate(), req.getAmount(), req.getCategory(), req.getType(), req.getDescription());
        when(transactionService.update(1L, 1L, req)).thenReturn(resp);

        ResponseEntity<TransactionResponse> response = transactionController.update(userPrincipal, 1L, req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
        verify(transactionService, times(1)).update(1L, 1L, req);
    }

    @Test
    void testDeleteTransaction() {
        doNothing().when(transactionService).delete(1L, 1L);

        ResponseEntity<?> response = transactionController.delete(userPrincipal, 1L);

        assertEquals(204, response.getStatusCode().value());
        verify(transactionService, times(1)).delete(1L, 1L);
    }
}
