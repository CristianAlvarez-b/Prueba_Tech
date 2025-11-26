package com.davivienda.app.controller;


import com.davivienda.app.dto.transaction.TransactionRequest;
import com.davivienda.app.dto.transaction.TransactionResponse;
import com.davivienda.app.security.UserPrincipal;
import com.davivienda.app.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {


    private final TransactionService transactionService;


    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal UserPrincipal user,
                                                      @Valid @RequestBody TransactionRequest req) {
        TransactionResponse resp = transactionService.create(user.getId(), req);
        return ResponseEntity.status(201).body(resp);
    }


    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(@AuthenticationPrincipal UserPrincipal user,
                                                          @RequestParam Map<String, String> filters,
                                                          Pageable pageable) {
        Page<TransactionResponse> page = transactionService.findAll(user.getId(), filters, pageable);
        return ResponseEntity.ok(page);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> get(@AuthenticationPrincipal UserPrincipal user,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(user.getId(), id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@AuthenticationPrincipal UserPrincipal user,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.ok(transactionService.update(user.getId(), id, req));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserPrincipal user,
                                    @PathVariable Long id) {
        transactionService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}