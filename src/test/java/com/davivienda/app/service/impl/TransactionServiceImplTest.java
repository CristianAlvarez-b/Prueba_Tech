package com.davivienda.app.service.impl;

import com.davivienda.app.dto.transaction.TransactionRequest;
import com.davivienda.app.dto.transaction.TransactionResponse;
import com.davivienda.app.entity.Transaction;
import com.davivienda.app.entity.User;
import com.davivienda.app.repository.TransactionRepository;
import com.davivienda.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository txRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------- CREATE --------------------
    @Test
    void testCreate_success() {
        User user = new User();
        user.setId(1L);

        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.now());
        req.setAmount(BigDecimal.valueOf(100));
        req.setCategory("Food");
        req.setType("INCOME");
        req.setDescription("Lunch");

        Transaction tx = Transaction.builder()
                .id(1L)
                .user(user)
                .date(req.getDate())
                .amount(req.getAmount())
                .category(req.getCategory())
                .type(req.getType())
                .description(req.getDescription())
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(txRepo.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse resp = transactionService.create(1L, req);

        assertEquals(1L, resp.getId());
        assertEquals("Food", resp.getCategory());
        verify(txRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreate_userNotFound() {
        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.now());
        req.setAmount(BigDecimal.valueOf(100));
        req.setCategory("Food");
        req.setType("INCOME");
        req.setDescription("Lunch");

        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> transactionService.create(1L, req));
        assertEquals("User not found", ex.getMessage());
    }

    // -------------------- UPDATE --------------------
    @Test
    void testUpdate_success() {
        User user = new User();
        user.setId(1L);

        Transaction tx = Transaction.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .amount(BigDecimal.valueOf(50))
                .category("Food")
                .type("EXPENSE")
                .description("Breakfast")
                .build();

        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.now());
        req.setAmount(BigDecimal.valueOf(70));
        req.setCategory("Food");
        req.setType("EXPENSE");
        req.setDescription("Brunch");

        when(txRepo.findById(1L)).thenReturn(Optional.of(tx));
        when(txRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse resp = transactionService.update(1L, 1L, req);

        assertEquals(BigDecimal.valueOf(70), resp.getAmount());
        assertEquals("Brunch", resp.getDescription());
        verify(txRepo, times(1)).save(any(Transaction.class));
    }

    @Test
    void testUpdate_txNotFound() {
        when(txRepo.findById(1L)).thenReturn(Optional.empty());

        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.now());
        req.setAmount(BigDecimal.valueOf(100));
        req.setCategory("Food");
        req.setType("INCOME");
        req.setDescription("Lunch");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> transactionService.update(1L, 1L, req));
        assertEquals("Tx not found", ex.getMessage());
    }

    @Test
    void testUpdate_forbidden() {
        Transaction tx2 = Transaction.builder().id(1L).user(User.builder().id(2L).build()).build();
        when(txRepo.findById(1L)).thenReturn(Optional.of(tx2));

        TransactionRequest req = new TransactionRequest();
        req.setDate(LocalDate.now());
        req.setAmount(BigDecimal.valueOf(100));
        req.setCategory("Food");
        req.setType("INCOME");
        req.setDescription("Lunch");

        Exception ex = assertThrows(SecurityException.class, () -> transactionService.update(1L, 1L, req));
        assertEquals("Forbidden", ex.getMessage());
    }

    // -------------------- DELETE --------------------
    @Test
    void testDelete_success() {
        User user = new User();
        user.setId(1L);

        Transaction tx = Transaction.builder().id(1L).user(user).build();

        when(txRepo.findById(1L)).thenReturn(Optional.of(tx));

        transactionService.delete(1L, 1L);

        verify(txRepo, times(1)).delete(tx);
    }

    @Test
    void testDelete_txNotFound() {
        when(txRepo.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> transactionService.delete(1L, 1L));
        assertEquals("Tx not found", ex.getMessage());
    }

    @Test
    void testDelete_forbidden() {
        Transaction tx2 = Transaction.builder().id(1L).user(User.builder().id(2L).build()).build();
        when(txRepo.findById(1L)).thenReturn(Optional.of(tx2));

        Exception ex = assertThrows(SecurityException.class, () -> transactionService.delete(1L, 1L));
        assertEquals("Forbidden", ex.getMessage());
    }

    // -------------------- GET BY ID --------------------
    @Test
    void testGetById_success() {
        User user = new User();
        user.setId(1L);

        Transaction tx = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(BigDecimal.valueOf(100))
                .category("Food")
                .type("INCOME")
                .description("Lunch")
                .date(LocalDate.now())
                .build();

        when(txRepo.findById(1L)).thenReturn(Optional.of(tx));

        TransactionResponse resp = transactionService.getById(1L, 1L);

        assertEquals(1L, resp.getId());
        assertEquals("Food", resp.getCategory());
    }

    @Test
    void testGetById_txNotFound() {
        when(txRepo.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> transactionService.getById(1L, 1L));
        assertEquals("Tx not found", ex.getMessage());
    }

    @Test
    void testGetById_forbidden() {
        Transaction tx2 = Transaction.builder().id(1L).user(User.builder().id(2L).build()).build();
        when(txRepo.findById(1L)).thenReturn(Optional.of(tx2));

        Exception ex = assertThrows(SecurityException.class, () -> transactionService.getById(1L, 1L));
        assertEquals("Forbidden", ex.getMessage());
    }

    // -------------------- FIND ALL --------------------
    @Test
    void testFindAll_unsortedPageable() {
        User user = new User();
        user.setId(1L);

        Transaction tx1 = Transaction.builder().id(1L).user(user).amount(BigDecimal.valueOf(50))
                .date(LocalDate.now()).category("Food").type("EXPENSE").description("Breakfast").build();
        Page<Transaction> page = new PageImpl<>(List.of(tx1));

        when(txRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10); // unsorted
        Page<TransactionResponse> result = transactionService.findAll(1L, Collections.emptyMap(), pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindAll_sortedPageable() {
        User user = new User();
        user.setId(1L);

        Transaction tx1 = Transaction.builder().id(1L).user(user).amount(BigDecimal.valueOf(50))
                .date(LocalDate.now()).category("Food").type("EXPENSE").description("Breakfast").build();
        Page<Transaction> page = new PageImpl<>(List.of(tx1));

        when(txRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("date")); // already sorted
        Page<TransactionResponse> result = transactionService.findAll(1L, Collections.emptyMap(), pageable);

        assertEquals(1, result.getTotalElements());
    }
}
