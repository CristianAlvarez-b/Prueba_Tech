package com.davivienda.app.service.impl;


import com.davivienda.app.dto.transaction.TransactionRequest;
import com.davivienda.app.dto.transaction.TransactionResponse;
import com.davivienda.app.entity.Transaction;
import com.davivienda.app.entity.User;
import com.davivienda.app.repository.TransactionRepository;
import com.davivienda.app.repository.UserRepository;
import com.davivienda.app.service.TransactionService;
import com.davivienda.app.util.TransactionFilters;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {


    private final TransactionRepository txRepo;
    private final UserRepository userRepo;


    public TransactionServiceImpl(TransactionRepository txRepo, UserRepository userRepo) {
        this.txRepo = txRepo;
        this.userRepo = userRepo;
    }


    @Override
    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest req) {
        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Transaction tx = Transaction.builder()
                .user(user)
                .date(req.getDate())
                .amount(req.getAmount())
                .category(req.getCategory())
                .type(req.getType())
                .description(req.getDescription())
                .build();
        tx = txRepo.save(tx);
        return mapToResponse(tx);
    }
    @Override
    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest req) {
        Transaction tx = txRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Tx not found"));
        if (!tx.getUser().getId().equals(userId)) throw new SecurityException("Forbidden");
        tx.setDate(req.getDate());
        tx.setAmount(req.getAmount());
        tx.setCategory(req.getCategory());
        tx.setType(req.getType());
        tx.setDescription(req.getDescription());
        tx = txRepo.save(tx);
        return mapToResponse(tx);
    }


    @Override
    public void delete(Long userId, Long id) {
        Transaction tx = txRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Tx not found"));
        if (!tx.getUser().getId().equals(userId)) throw new SecurityException("Forbidden");
        txRepo.delete(tx);
    }


    @Override
    public TransactionResponse getById(Long userId, Long id) {
        Transaction tx = txRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Tx not found"));
        if (!tx.getUser().getId().equals(userId)) throw new SecurityException("Forbidden");
        return mapToResponse(tx);
    }
    @Override
    public Page<TransactionResponse> findAll(Long userId, Map<String, String> filters, Pageable pageable) {
        Specification<Transaction> spec = TransactionFilters.filterBy(userId, filters);
// Si no viene sort en pageable, aplicar orden por fecha desc por defecto
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "date"));
        }
        Page<Transaction> page = txRepo.findAll(spec, pageable);
        return page.map(this::mapToResponse);
    }


    private TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(tx.getId(), tx.getDate(), tx.getAmount(), tx.getCategory(), tx.getType(), tx.getDescription());
    }
}