package com.davivienda.app.service;

import com.davivienda.app.dto.transaction.TransactionRequest;
import com.davivienda.app.dto.transaction.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Map;


public interface TransactionService {
    TransactionResponse create(Long userId, TransactionRequest req);
    TransactionResponse update(Long userId, Long id, TransactionRequest req);
    void delete(Long userId, Long id);
    TransactionResponse getById(Long userId, Long id);
    Page<TransactionResponse> findAll(Long userId, Map<String, String> filters, Pageable pageable);
}
